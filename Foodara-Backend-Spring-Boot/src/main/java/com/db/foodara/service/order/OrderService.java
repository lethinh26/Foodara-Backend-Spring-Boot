package com.db.foodara.service.order;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.db.foodara.dto.request.order.RejectOrderRequest;
import com.db.foodara.dto.response.merchant.MerchantOrderItemResponse;
import com.db.foodara.dto.response.merchant.MerchantOrderResponse;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.order.OrderItem;
import com.db.foodara.entity.order.OrderItemOption;
import com.db.foodara.entity.order.OrderStatusHistory;
import com.db.foodara.entity.user.User;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.repository.order.OrderItemOptionRepository;
import com.db.foodara.repository.order.OrderItemRepository;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.order.OrderStatusHistoryRepository;
import com.db.foodara.repository.store.StoreRepository;
import com.db.foodara.repository.user.UserRepository;
import com.db.foodara.service.promotion.VoucherUsageService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * M05–M07 — Merchant order lifecycle:
 *   pending → confirmed → ready_for_pickup → picked_up → delivered
 * (or cancelled at any merchant-controlled step).
 *
 * Publishes domain events to RabbitMQ for Notification Service.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final MerchantRepository merchantRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final UserRepository userRepository;
    private final OrderInventoryService orderInventoryService;
    private final VoucherUsageService voucherUsageService;
    private final OrderEventPublisher eventPublisher;

    public List<MerchantOrderResponse> getOrders(String userId, String storeId) {
        ensureMerchantOwnsStore(userId, storeId);
        return orderRepository.findByStoreIdOrderByPlacedAtDesc(storeId).stream()
                .filter(o -> !("qr".equalsIgnoreCase(o.getPaymentMethod())
                        && "pending".equalsIgnoreCase(o.getPaymentStatus())))
                .map(this::mapToResponse)
                .toList();
    }

    public MerchantOrderResponse getOrderDetail(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);
        return mapToResponse(order);
    }

    @Transactional
    public MerchantOrderResponse acceptOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);
        MerchantOrderResponse response = transition(order, "confirmed", userId, "Merchant confirmed", o -> {
            LocalDateTime now = LocalDateTime.now();
            o.setPreparingAt(now);
            o.setStoreRespondedAt(now);
        });
        // Publish event
        eventPublisher.publish(Map.of(
            "orderId", order.getId(),
            "orderNumber", order.getOrderNumber(),
            "storeId", order.getStoreId(),
            "storeName", order.getStoreName(),
            "customerId", order.getCustomerId(),
            "newStatus", "confirmed",
            "timestamp", LocalDateTime.now().toString()
        ), "order.status");
        return response;
    }

    @Transactional
    public MerchantOrderResponse rejectOrder(String userId, String storeId, String orderId, RejectOrderRequest request) {
        Order order = validateAndGetOrder(userId, storeId, orderId);
        String reason = request != null ? request.getReason() : null;
        // DB CHECK constraint: cancelled_by IN ('customer','store','driver','admin','system')
        order.setCancelledBy("store");
        order.setCancellationReason(reason);
        MerchantOrderResponse response = transition(order, "cancelled", userId,
                "Merchant rejected: " + (reason != null ? reason : ""),
                o -> {
                    LocalDateTime now = LocalDateTime.now();
                    o.setCancelledAt(now);
                    o.setStoreRespondedAt(now);
                });

        // Restore stock that was reserved when the order was placed.
        orderInventoryService.restoreStockForOrder(orderId);
        // Refund any voucher slots used by the order.
        voucherUsageService.rollbackForOrder(orderId);

        // Notify notification service
        eventPublisher.publish(Map.of(
            "orderId", order.getId(),
            "orderNumber", order.getOrderNumber(),
            "customerId", order.getCustomerId(),
            "storeId", order.getStoreId(),
            "storeName", order.getStoreName(),
            "cancelledBy", "store"
        ), "order.cancelled");

        return response;
    }


    @Transactional
    public MerchantOrderResponse readyOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);
        MerchantOrderResponse response = transition(order, "ready_for_pickup", userId, "Food is ready for driver", o -> {
            LocalDateTime now = LocalDateTime.now();
            if (o.getPreparingAt() == null) o.setPreparingAt(now);
            o.setReadyAt(now);
        });
        eventPublisher.publish(Map.of(
            "orderId", order.getId(),
            "orderNumber", order.getOrderNumber(),
            "storeId", order.getStoreId(),
            "storeName", order.getStoreName(),
            "customerId", order.getCustomerId(),
            "oldStatus", "confirmed",
            "newStatus", "ready_for_pickup",
            "timestamp", LocalDateTime.now().toString()
        ), "order.status");
        return response;
    }

    @Transactional
    public MerchantOrderResponse handoverOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);
        MerchantOrderResponse response = transition(order, "picked_up", userId, "Handed over to driver", o -> o.setPickedUpAt(LocalDateTime.now()));
        eventPublisher.publish(Map.of(
            "orderId", order.getId(),
            "orderNumber", order.getOrderNumber(),
            "storeId", order.getStoreId(),
            "storeName", order.getStoreName(),
            "customerId", order.getCustomerId(),
            "oldStatus", "ready_for_pickup",
            "newStatus", "picked_up",
            "timestamp", LocalDateTime.now().toString()
        ), "order.status");
        return response;
    }



    // ---------- Internal helpers ----------

    private MerchantOrderResponse transition(Order order, String newStatus, String userId, String note,
                                             java.util.function.Consumer<Order> timestampSetter) {
        String oldStatus = order.getStatus();
        timestampSetter.accept(order);
        order.setStatus(newStatus);
        saveStatusHistory(order, oldStatus, newStatus, userId, note);
        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    private void ensureMerchantOwnsStore(String userId, String storeId) {
        var merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        var store = storeRepository.findStoreById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        if (!merchant.getId().equals(store.getMerchantId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private Order validateAndGetOrder(String userId, String storeId, String orderId) {
        ensureMerchantOwnsStore(userId, storeId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getStoreId().equals(storeId)) {
            throw new AppException(ErrorCode.WRONG_ORDER);
        }
        return order;
    }

    private void saveStatusHistory(Order order, String fromStatus, String toStatus, String userId, String note) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(userId);
        history.setChangedByRole("merchant");
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(history);
    }

    private MerchantOrderResponse mapToResponse(Order o) {
        User customer = o.getCustomerId() != null
                ? userRepository.findById(o.getCustomerId()).orElse(null)
                : null;

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(o.getId());
        List<String> orderItemIds = orderItems.stream().map(OrderItem::getId).collect(Collectors.toList());
        Map<String, List<OrderItemOption>> optionsByItem = orderItemIds.isEmpty()
                ? Collections.emptyMap()
                : orderItemOptionRepository.findByOrderItem_IdIn(orderItemIds).stream()
                        .collect(Collectors.groupingBy(opt -> opt.getOrderItem().getId()));

        List<MerchantOrderItemResponse> items = orderItems.stream()
                .map(it -> mapItem(it, optionsByItem.getOrDefault(it.getId(), Collections.emptyList())))
                .toList();

        return MerchantOrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .storeId(o.getStoreId())
                .customerId(o.getCustomerId())
                .customerName(customer != null ? customer.getFullName() : null)
                .customerPhone(customer != null ? customer.getPhone() : null)
                .driverId(o.getDriverId())
                .status(o.getStatus() != null ? o.getStatus().toLowerCase() : null)
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .subtotal(o.getSubtotal())
                .deliveryFee(o.getDeliveryFee())
                .storeDiscount(o.getStoreDiscount())
                .voucherDiscount(o.getVoucherDiscount())
                .totalAmount(o.getTotalAmount())
                .pickupCode(o.getPickupCode())
                .deliveryNote(o.getDeliveryNote())
                .cancellationReason(o.getCancellationReason())
                .items(items)
                .placedAt(o.getPlacedAt())
                .confirmedAt(o.getConfirmedAt())
                .preparingAt(o.getPreparingAt())
                .readyAt(o.getReadyAt())
                .pickedUpAt(o.getPickedUpAt())
                .deliveredAt(o.getDeliveredAt())
                .cancelledAt(o.getCancelledAt())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private MerchantOrderItemResponse mapItem(OrderItem item, List<OrderItemOption> options) {
        List<MerchantOrderItemResponse.OptionResponse> optionResponses = options.stream()
                .map(opt -> MerchantOrderItemResponse.OptionResponse.builder()
                        .optionItemId(opt.getOptionItemId())
                        .groupName(opt.getOptionGroupName())
                        .optionName(opt.getOptionName())
                        .priceAdjustment(opt.getPriceAdjustment())
                        .build())
                .toList();

        return MerchantOrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .comboId(item.getComboId())
                .name(item.getItemName())
                .imageUrl(item.getItemImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .note(item.getSpecialInstructions())
                .options(optionResponses.isEmpty() ? null : optionResponses)
                .build();
    }
}
