package com.db.foodara.service.order;

import com.db.foodara.config.SepayConfig;
import com.db.foodara.dto.request.order.CheckoutPreviewRequest;
import com.db.foodara.dto.request.order.PlaceOrderRequest;
import com.db.foodara.dto.response.order.CheckoutPreviewResponse;
import com.db.foodara.dto.response.order.PlaceOrderResponse;
import com.db.foodara.entity.order.*;
import com.db.foodara.entity.store.Store;
import com.db.foodara.entity.user.UserAddress;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.order.*;
import com.db.foodara.repository.store.StoreRepository;
import com.db.foodara.repository.user.UserAddressRepository;
import com.db.foodara.repository.user.UserRepository;
import com.db.foodara.service.payment.SepayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final CheckoutService checkoutService;
    private final SepayService sepayService;
    private final SepayConfig sepayConfig;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    /**
     * Place a new order from customer's cart
     */
    @Transactional
    public PlaceOrderResponse placeOrder(String userId, PlaceOrderRequest request) {
        // 1. Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. Validate store
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        // 3. Validate address
        UserAddress address = userAddressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        // 4. Get cart and items
        Cart cart = cartRepository.findByUserIdAndStoreId(userId, request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        List<CartItem> cartItems = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_NOT_FOUND);
        }

        // 5. Calculate pricing using CheckoutService
        CheckoutPreviewRequest previewRequest = new CheckoutPreviewRequest();
        previewRequest.setStoreId(request.getStoreId());
        previewRequest.setAddressId(request.getAddressId());
        previewRequest.setPlatformVoucherId(request.getPlatformVoucherId());
        previewRequest.setStoreVoucherId(request.getStoreVoucherId());
        previewRequest.setPlatformCode(request.getPlatformCode());
        previewRequest.setStoreCode(request.getStoreCode());
        CheckoutPreviewResponse preview = checkoutService.preview(userId, previewRequest);

        // 6. Generate order number
        String orderNumber = generateOrderNumber();

        // 7. Create Order entity
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerId(userId);
        order.setStoreId(request.getStoreId());
        order.setStatus("pending");
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus("pending");

        // Delivery
        order.setDeliveryAddressId(request.getAddressId());
        order.setDeliveryAddressSnapshot(buildAddressSnapshot(address));
        order.setDeliveryLatitude(address.getLatitude());
        order.setDeliveryLongitude(address.getLongitude());
        order.setDeliveryNote(request.getNote());

        // Store snapshot
        order.setStoreName(store.getName());
        order.setStoreAddress(store.getAddressLine());
        order.setStoreLatitude(store.getLatitude());
        order.setStoreLongitude(store.getLongitude());

        // Pricing
        order.setSubtotal(preview.getSubtotal());
        order.setDeliveryFee(preview.getDeliveryFee());
        order.setPlatformFee(preview.getPlatformFee());
        order.setVoucherDiscount(preview.getTotalDiscount());
        order.setStoreDiscount(preview.getStoreDiscount());
        order.setTotalAmount(preview.getTotalAmount());

        // Vouchers
        if (preview.getAppliedPlatformVoucher() != null) {
            order.setPlatformVoucherId(preview.getAppliedPlatformVoucher().getVoucherId());
        }
        if (preview.getAppliedStoreVoucher() != null) {
            order.setStoreVoucherId(preview.getAppliedStoreVoucher().getVoucherId());
        }

        // Timestamps
        order.setPlacedAt(LocalDateTime.now());
        order.setEstimatedPrepTime(20);
        order.setEstimatedDeliveryTime(15);
        order.setEstimatedTotalTime(35);

        Order savedOrder = orderRepository.save(order);

        // 8. Copy cart items to order items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItemId(cartItem.getMenuItemId());
            orderItem.setComboId(cartItem.getComboId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setTotalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItem.setSpecialInstructions(cartItem.getSpecialInstructions());
            // itemName will be populated if available, otherwise use menuItemId
            orderItem.setItemName("Menu Item #" + cartItem.getMenuItemId());
            orderItemRepository.save(orderItem);
        }

        // 9. Create initial status history
        saveStatusHistory(savedOrder, null, "pending", userId, "CUSTOMER", "Đặt đơn thành công");

        // 10. Clear cart after order placed
        cartItemRepository.deleteAll(cartItems);
        cartRepository.delete(cart);

        // 11. Handle SePay checkout for QR payment
        String checkoutUrl = null;
        if ("qr".equalsIgnoreCase(request.getPaymentMethod())) {
            String backendBaseUrl = "http://localhost:8080/api"; // TODO: make configurable
            String successUrl = backendBaseUrl + "/v1/payment/sepay/callback?orderId=" + savedOrder.getId() + "&status=success";
            String errorUrl = backendBaseUrl + "/v1/payment/sepay/callback?orderId=" + savedOrder.getId() + "&status=error";
            String cancelUrl = backendBaseUrl + "/v1/payment/sepay/callback?orderId=" + savedOrder.getId() + "&status=cancel";

            try {
                checkoutUrl = sepayService.createCheckout(
                        savedOrder.getId(),
                        orderNumber,
                        preview.getTotalAmount(),
                        "Thanh toán đơn hàng " + orderNumber + " - Foodara",
                        successUrl, errorUrl, cancelUrl
                );
            } catch (Exception e) {
                log.error("Failed to create SePay checkout for order {}", orderNumber, e);
                // Still return order — customer can retry payment
            }
        }

        // 12. Build response
        return PlaceOrderResponse.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .status(savedOrder.getStatus())
                .paymentMethod(savedOrder.getPaymentMethod())
                .paymentStatus(savedOrder.getPaymentStatus())
                .subtotal(savedOrder.getSubtotal())
                .deliveryFee(savedOrder.getDeliveryFee())
                .platformFee(savedOrder.getPlatformFee())
                .voucherDiscount(savedOrder.getVoucherDiscount())
                .totalAmount(savedOrder.getTotalAmount())
                .checkoutUrl(checkoutUrl)
                .placedAt(savedOrder.getPlacedAt())
                .estimatedDeliveryTime(savedOrder.getEstimatedTotalTime())
                .build();
    }

    /**
     * Get customer's order list
     */
    public List<Order> getCustomerOrders(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId);
        orders.forEach(this::enrichOrderWithStoreInfo);
        return orders;
    }

    /**
     * Get order detail for a customer
     */
    public Order getCustomerOrderDetail(String userId, String orderId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        enrichOrderWithStoreInfo(order);
        return order;
    }

    private void enrichOrderWithStoreInfo(Order order) {
        storeRepository.findById(order.getStoreId()).ifPresent(store -> {
            order.setStoreLogoUrl(store.getLogoUrl());
            order.setStorePhone(store.getPhone());
        });
    }

    /**
     * Cancel an order (only when status is PENDING)
     */
    @Transactional
    public Order cancelOrder(String userId, String orderId, String reason) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!"pending".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_CANNOT_CANCEL);
        }

        String oldStatus = order.getStatus();
        order.setStatus("cancelled");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy("CUSTOMER");
        order.setCancellationReason(reason != null ? reason : "Customer cancelled");

        saveStatusHistory(order, oldStatus, "cancelled", userId, "CUSTOMER", "Khách huỷ đơn: " + (reason != null ? reason : ""));
        return orderRepository.save(order);
    }

    /**
     * Update payment status (called by IPN handler)
     */
    @Transactional
    public void updatePaymentStatus(String orderNumber, String paymentStatus) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setPaymentStatus(paymentStatus);
        orderRepository.save(order);
        log.info("Updated payment status for order {} to {}", orderNumber, paymentStatus);
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomPart = String.format("%03d", (int) (Math.random() * 999) + 1);
        return "FD-" + datePart + "-" + randomPart;
    }

    private String buildAddressSnapshot(UserAddress address) {
        // Must return valid JSON since column is jsonb
        String line = address.getAddressLine() != null ? address.getAddressLine() : "";
        String ward = address.getWard() != null ? address.getWard() : "";
        String district = address.getDistrictName() != null ? address.getDistrictName() : "";
        String city = address.getCityName() != null ? address.getCityName() : "";
        String name = address.getRecipientName() != null ? address.getRecipientName() : "";
        String phone = address.getRecipientPhone() != null ? address.getRecipientPhone() : "";
        return String.format("{\"addressLine\":\"%s\",\"ward\":\"%s\",\"district\":\"%s\",\"city\":\"%s\",\"name\":\"%s\",\"phone\":\"%s\"}",
                escapeJson(line), escapeJson(ward), escapeJson(district), escapeJson(city), escapeJson(name), escapeJson(phone));
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void saveStatusHistory(Order order, String fromStatus, String toStatus,
                                    String changedBy, String role, String note) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(fromStatus != null ? fromStatus : "");
        history.setToStatus(toStatus);
        history.setChangedBy(changedBy);
        history.setChangedByRole(role);
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(history);
    }
}
