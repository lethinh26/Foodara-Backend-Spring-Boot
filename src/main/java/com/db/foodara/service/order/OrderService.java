package com.db.foodara.service.order;

import com.db.foodara.dto.request.order.RejectOrderRequest;
import com.db.foodara.dto.response.order.OrderResponseDTO;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.order.OrderItem;
import com.db.foodara.entity.order.OrderStatusHistory;
import com.db.foodara.entity.store.MenuItem;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.repository.order.OrderAssignmentRepository;
import com.db.foodara.repository.order.OrderItemRepository;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.order.OrderStatusHistoryRepository;
import com.db.foodara.repository.store.MenuItemRepository;
import com.db.foodara.repository.store.StoreRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private MenuItemRepository menuItemRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderAssignmentRepository orderAssignmentRepository;

    // 114. Lấy danh sách đơn hàng cho Merchant
    public List<OrderResponseDTO> getOrders(String userId, String storeId) {
        validateMerchantAndStore(userId, storeId);
        List<Order> orders = orderRepository.findByStoreId(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return orders.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // 115. Chi tiết đơn hàng
    public OrderResponseDTO getOrderDetail(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);
        return mapToResponseDTO(order);
    }

    // 116. Chấp nhận đơn
    @Transactional
    public OrderResponseDTO acceptOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);

        String oldStatus = order.getStatus();
        order.setStatus("preparing"); // Đồng bộ với OrderStatus bên TS (lowercase)
        order.setConfirmedAt(LocalDateTime.now());

        saveStatusHistory(order, oldStatus, "preparing", userId, "Merchant accepted order");
        return mapToResponseDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO completedOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);

        String oldStatus = order.getStatus();
        order.setStatus("completed"); // Đồng bộ với OrderStatus bên TS (lowercase)
        order.setConfirmedAt(LocalDateTime.now());

        saveStatusHistory(order, oldStatus, "completed", userId, "Merchant accepted order");
        return mapToResponseDTO(orderRepository.save(order));
    }

    //117	PUT	/api/merchant/orders/:id/reject	Từ chối đơn (kèm lý do)
    @Transactional
    public Order rejectOrder(String userId, String storeId, String orderId, RejectOrderRequest request) {
        Order order = validateAndGetOrder(userId, storeId, orderId);

        String oldStatus = order.getStatus();
        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy("MERCHANT");
        order.setCancellationReason(request.getReason());

        saveStatusHistory(order, oldStatus, "CANCELLED", userId, "Merchant rejected: " + request.getReason());
        return orderRepository.save(order);
    }

    //118	PUT	/api/merchant/orders/:id/preparing	Chuyển sang "đang chuẩn bị"
    @Transactional
    public Order preparingOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);

        String oldStatus = order.getStatus();
        order.setStatus("PREPARING");
        order.setPreparingAt(LocalDateTime.now());

        saveStatusHistory(order, oldStatus, "PREPARING", userId, "Kitchen started preparing");
        return orderRepository.save(order);
    }

    //119	PUT	/api/merchant/orders/:id/ready	Đánh dấu "sẵn sàng lấy hàng"
    @Transactional
    public Order readyOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);

        String oldStatus = order.getStatus();
        order.setStatus("ready_for_pickup");
        order.setReadyAt(LocalDateTime.now());

        saveStatusHistory(order, oldStatus, "ready_for_pickup", userId, "Food is ready for driver");
        return orderRepository.save(order);
    }

    //120	PUT	/api/merchant/orders/:id/handover	Xác nhận giao cho tài xế
    @Transactional
    public Order handoverOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);

        String oldStatus = order.getStatus();
        order.setStatus("picked_up");
        order.setPickedUpAt(LocalDateTime.now());

        saveStatusHistory(order, oldStatus, "picked_up", userId, "Handed over to driver");
        return orderRepository.save(order);
    }


    //121	WS	/ws/merchant/orders	WebSocket nhận đơn mới realtime
    // cai nay la sao nhowf????

    private Order validateAndGetOrder(String userId, String storeId, String orderId) {
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        storeRepository.findStoreById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

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
        history.setChangedByRole("MERCHANT");
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(history);
    }

    // --- Hàm Helper Mapping ---
    private OrderResponseDTO mapToResponseDTO(Order order) {
        // Bạn có thể dùng MapStruct ở đây, dưới đây là map tay ví dụ:
        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .storeId(order.getStoreId())
                .storeName(order.getStoreName())
                // Map pricing khớp với interface CheckoutPricing
                .pricing(OrderResponseDTO.PricingDTO.builder()
                        .subtotal(order.getSubtotal())
                        .deliveryFee(order.getDeliveryFee())
                        .platformFee(order.getPlatformFee())
                        .discount(order.getStoreDiscount())
                        .total(order.getTotalAmount())
                        .build())
                .items(orderItemRepository.findByOrderId(order.getId()).stream()
                        .map(item -> OrderResponseDTO.OrderItemResponseDTO.builder()
                                .id(item.getMenuItemId())
                                .menuItemId(item.getMenuItemId())
                                .image(item.getItemImageUrl())
                                .name(item.getItemName())
                                .quantity(item.getQuantity())
                                .note(item.getSpecialInstructions())
                                .comboId(item.getComboId())
                                .price(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .selectedOptions(item.getOptionsSnapshot())
                                .build())
                        .toList())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private void validateMerchantAndStore(String userId, String storeId) {
        merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        storeRepository.findStoreById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
    }

}
