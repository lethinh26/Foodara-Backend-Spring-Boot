package com.db.foodara.service.order;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.db.foodara.client.PaymentServiceClient;
import com.db.foodara.dto.request.order.CheckoutPreviewRequest;
import com.db.foodara.dto.request.order.PlaceOrderRequest;
import com.db.foodara.dto.response.order.CheckoutPreviewResponse;
import com.db.foodara.dto.response.order.PlaceOrderResponse;
import com.db.foodara.entity.order.Cart;
import com.db.foodara.entity.order.CartItem;
import com.db.foodara.entity.order.CartItemOption;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.order.OrderItem;
import com.db.foodara.entity.order.OrderItemOption;
import com.db.foodara.entity.order.OrderStatusHistory;
import com.db.foodara.entity.store.Combo;
import com.db.foodara.entity.store.MenuItem;
import com.db.foodara.entity.store.OptionGroup;
import com.db.foodara.entity.store.OptionItem;
import com.db.foodara.entity.store.Store;
import com.db.foodara.entity.user.UserAddress;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.order.CartItemOptionRepository;
import com.db.foodara.repository.order.CartItemRepository;
import com.db.foodara.repository.order.CartRepository;
import com.db.foodara.repository.order.OrderItemOptionRepository;
import com.db.foodara.repository.order.OrderItemRepository;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.order.OrderStatusHistoryRepository;
import com.db.foodara.repository.store.ComboRepository;
import com.db.foodara.repository.store.MenuItemRepository;
import com.db.foodara.repository.store.OptionGroupRepository;
import com.db.foodara.repository.store.OptionItemRepository;
import com.db.foodara.repository.store.StoreRepository;
import com.db.foodara.repository.user.UserAddressRepository;
import com.db.foodara.repository.user.UserRepository;
import com.db.foodara.service.websocket.WebSocketNotificationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PICKUP_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no I/O/0/1
    private static final int STORE_RESPONSE_TIMEOUT_MINUTES = 5;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionRepository cartItemOptionRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final MenuItemRepository menuItemRepository;
    private final ComboRepository comboRepository;
    private final OptionItemRepository optionItemRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final CheckoutService checkoutService;
    private final PaymentServiceClient paymentServiceClient;
    private final WebSocketNotificationService webSocketNotificationService;
    private final OrderInventoryService orderInventoryService;
    private final com.db.foodara.service.promotion.VoucherUsageService voucherUsageService;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;


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

        // 5.5 Reserve stock — throws MENU_ITEM_OUT_OF_STOCK and rolls back if any item runs out
        List<OrderInventoryService.CartLine> stockLines = cartItems.stream()
                .map(ci -> (OrderInventoryService.CartLine) new OrderInventoryService.CartLine() {
                    @Override public String menuItemId() { return ci.getMenuItemId(); }
                    @Override public int quantity() { return ci.getQuantity() != null ? ci.getQuantity() : 0; }
                })
                .toList();
        orderInventoryService.reserveStockForCart(stockLines);

        // 6. Generate order number
        String orderNumber = generateOrderNumber();

        // 7. Create Order entity
        LocalDateTime placedAt = LocalDateTime.now();
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

        // Commission — copied from store at order time so historical commissions stay correct
        // even if the store later renegotiates its rate.
        if (store.getCommissionRate() != null) {
            order.setCommissionRate(store.getCommissionRate());
            order.setCommissionAmount(preview.getSubtotal()
                    .multiply(store.getCommissionRate())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
        }

        // Delivery distance — Haversine between store and delivery address (in km, 2 decimals)
        BigDecimal distanceKm = haversineKm(
                store.getLatitude(), store.getLongitude(),
                address.getLatitude(), address.getLongitude());
        if (distanceKm != null) {
            order.setDeliveryDistanceKm(distanceKm);
        }

        // Vouchers
        if (preview.getAppliedPlatformVoucher() != null) {
            order.setPlatformVoucherId(preview.getAppliedPlatformVoucher().getVoucherId());
        }
        if (preview.getAppliedStoreVoucher() != null) {
            order.setStoreVoucherId(preview.getAppliedStoreVoucher().getVoucherId());
        }

        // Timestamps & deadlines
        order.setPlacedAt(placedAt);
        order.setStoreResponseDeadline(placedAt.plusMinutes(STORE_RESPONSE_TIMEOUT_MINUTES));
        order.setEstimatedPrepTime(20);
        order.setEstimatedDeliveryTime(15);
        order.setEstimatedTotalTime(35);

        // Pickup code (merchant -> driver) and OTP (customer -> driver)
        order.setPickupCode(generatePickupCode());
        order.setDeliveryOtp(generateDeliveryOtp());
        order.setIsReorder(Boolean.FALSE);

        // Persist + flush immediately so child writes (order_items, voucher_usage,
        // user_vouchers.order_id, status history) see the parent row in the DB and
        // satisfy their FOREIGN KEY constraints.
        Order savedOrder = orderRepository.saveAndFlush(order);

        // 8. Snapshot cart items -> order items + order_item_options + options_snapshot JSON
        snapshotCartToOrder(savedOrder, cartItems);

        // 9. Create initial status history
        saveStatusHistory(savedOrder, null, "pending", userId, "CUSTOMER", "Đặt đơn thành công");

        // 9.5 Record voucher usage (atomic counter + voucher_usage row + user_vouchers flag)
        if (preview.getAppliedPlatformVoucher() != null) {
            voucherUsageService.recordUsage(
                    savedOrder.getId(),
                    userId,
                    preview.getAppliedPlatformVoucher().getVoucherId(),
                    preview.getAppliedPlatformVoucher().getPotentialDiscount()
            );
        }
        if (preview.getAppliedStoreVoucher() != null) {
            voucherUsageService.recordUsage(
                    savedOrder.getId(),
                    userId,
                    preview.getAppliedStoreVoucher().getVoucherId(),
                    preview.getAppliedStoreVoucher().getPotentialDiscount()
            );
        }

        // 10. Clear cart after order placed (delete options first to satisfy FK)
        List<String> cartItemIds = cartItems.stream().map(CartItem::getId).collect(Collectors.toList());
        cartItemIds.forEach(cartItemOptionRepository::deleteByCartItemId);
        cartItemRepository.deleteAll(cartItems);
        cartRepository.delete(cart);

        // 11. Handle SePay checkout for QR payment
        String checkoutUrl = null;
        if ("qr".equalsIgnoreCase(request.getPaymentMethod())) {
            String gatewayBaseUrl = "http://localhost:8080/api"; // Callback goes through API Gateway → payment-service
            String successUrl = gatewayBaseUrl + "/v1/payment/sepay/callback?orderId=" + savedOrder.getId() + "&status=success";
            String errorUrl = gatewayBaseUrl + "/v1/payment/sepay/callback?orderId=" + savedOrder.getId() + "&status=error";
            String cancelUrl = gatewayBaseUrl + "/v1/payment/sepay/callback?orderId=" + savedOrder.getId() + "&status=cancel";

            try {
                checkoutUrl = paymentServiceClient.createCheckout(
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

        // 11.5 Notify merchant in real time so the inbox/kitchen UI can show the new order + sound
        try {
            webSocketNotificationService.sendNewOrderToMerchant(
                    savedOrder.getStoreId(),
                    buildMerchantNotification(savedOrder, store)
            );
        } catch (Exception e) {
            log.warn("Failed to push new-order notification for store {}: {}", savedOrder.getStoreId(), e.getMessage());
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
        // Populate the @Transient statusHistory so the customer timeline can render
        order.setStatusHistory(orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId));
        // Populate order items so the FE can display item details + review per item
        order.setOrderItems(orderItemRepository.findByOrderId(orderId));
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
        // DB CHECK: cancelled_by must be lowercase 'customer'
        order.setCancelledBy("customer");
        order.setCancellationReason(reason != null ? reason : "Customer cancelled");

        saveStatusHistory(order, oldStatus, "cancelled", userId, "CUSTOMER", "Khách huỷ đơn: " + (reason != null ? reason : ""));
        Order saved = orderRepository.save(order);

        // Restore stock for any tracked items so they become buyable again.
        orderInventoryService.restoreStockForOrder(saved.getId());
        // Refund the voucher slot + the user's wallet entry.
        voucherUsageService.rollbackForOrder(saved.getId());
        return saved;
    }

    /**
     * Reorder — copy every order_item + order_item_options of a previous order back into the
     * customer's active cart for that store. Returns the new cart id so the FE can navigate
     * straight to checkout.
     *
     * Endpoint: {@code POST /api/v1/orders/:id/reorder} (#72 in api_endpoints.md).
     */
    @Transactional
    public ReorderResult reorderFromOrder(String userId, String orderId) {
        Order original = orderRepository.findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Store store = storeRepository.findById(original.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        if (!Boolean.TRUE.equals(store.getIsActive())) {
            throw new AppException(ErrorCode.STORE_CLOSED);
        }

        // Reuse the user's existing cart for this store, otherwise create a fresh one
        Cart cart = cartRepository.findByUserIdAndStoreId(userId, original.getStoreId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    c.setStoreId(original.getStoreId());
                    return cartRepository.save(c);
                });

        // Pre-load every option row of the original order so we can copy them per item
        List<OrderItem> originalItems = orderItemRepository.findByOrderId(original.getId());
        List<String> originalItemIds = originalItems.stream().map(OrderItem::getId).collect(Collectors.toList());
        Map<String, List<OrderItemOption>> optionsByItem = originalItemIds.isEmpty()
                ? Collections.emptyMap()
                : orderItemOptionRepository.findByOrderItem_IdIn(originalItemIds).stream()
                        .collect(Collectors.groupingBy(opt -> opt.getOrderItem().getId()));

        // Skip items whose source menu_item / combo no longer exists or is inactive
        int copiedCount = 0;
        int skippedCount = 0;
        for (OrderItem oi : originalItems) {
            BigDecimal currentPrice = resolveCurrentUnitPrice(oi);
            if (currentPrice == null) {
                skippedCount++;
                continue;
            }

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMenuItemId(oi.getMenuItemId());
            cartItem.setComboId(oi.getComboId());
            cartItem.setQuantity(oi.getQuantity());
            cartItem.setUnitPrice(currentPrice);
            cartItem.setSpecialInstructions(oi.getSpecialInstructions());
            CartItem savedCartItem = cartItemRepository.save(cartItem);

            for (OrderItemOption opt : optionsByItem.getOrDefault(oi.getId(), Collections.emptyList())) {
                if (opt.getOptionItemId() == null) continue;
                CartItemOption co = new CartItemOption();
                co.setCartItem(savedCartItem);
                co.setOptionItemId(opt.getOptionItemId());
                co.setPriceAdjustment(opt.getPriceAdjustment() != null ? opt.getPriceAdjustment() : BigDecimal.ZERO);
                cartItemOptionRepository.save(co);
            }
            copiedCount++;
        }

        if (copiedCount == 0) {
            // Nothing copied → don't leave an empty cart hanging around
            cartRepository.delete(cart);
            throw new AppException(ErrorCode.MENU_ITEM_OUT_OF_STOCK);
        }

        return new ReorderResult(cart.getId(), original.getStoreId(), copiedCount, skippedCount);
    }

    /**
     * Look up the current unit price of a historical order line. Returns {@code null}
     * if the underlying menu_item / combo is gone or marked inactive.
     */
    private BigDecimal resolveCurrentUnitPrice(OrderItem oi) {
        if (StringUtils.hasText(oi.getMenuItemId())) {
            MenuItem mi = menuItemRepository.findById(oi.getMenuItemId()).orElse(null);
            if (mi == null) return null;
            if (Boolean.FALSE.equals(mi.getIsActive()) || Boolean.FALSE.equals(mi.getIsAvailable())) return null;
            return mi.getBasePrice();
        }
        if (StringUtils.hasText(oi.getComboId())) {
            Combo combo = comboRepository.findById(oi.getComboId()).orElse(null);
            if (combo == null) return null;
            if (Boolean.FALSE.equals(combo.getIsActive())) return null;
            return combo.getComboPrice();
        }
        return null;
    }

    /** Lightweight status payload returned by {@code GET /v1/orders/:id/status}. */
    public Map<String, Object> getOrderStatus(String userId, String orderId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", order.getId());
        payload.put("orderNumber", order.getOrderNumber());
        payload.put("status", order.getStatus());
        payload.put("paymentStatus", order.getPaymentStatus());
        payload.put("placedAt", order.getPlacedAt());
        payload.put("confirmedAt", order.getConfirmedAt());
        payload.put("preparingAt", order.getPreparingAt());
        payload.put("readyAt", order.getReadyAt());
        payload.put("pickedUpAt", order.getPickedUpAt());
        payload.put("deliveredAt", order.getDeliveredAt());
        payload.put("completedAt", order.getCompletedAt());
        payload.put("cancelledAt", order.getCancelledAt());
        payload.put("cancellationReason", order.getCancellationReason());
        return payload;
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

    // ============================================================
    // Snapshot helpers
    // ============================================================

    /**
     * Copy every cart line into order_items, persist matching order_item_options rows,
     * and mirror the option list into order_items.options_snapshot (JSONB) so a single
     * SELECT can render the order without a join.
     */
    private void snapshotCartToOrder(Order savedOrder, List<CartItem> cartItems) {
        // Bulk-load supporting entities so we do at most one query per kind
        Set<String> menuItemIds = cartItems.stream()
                .map(CartItem::getMenuItemId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Set<String> comboIds = cartItems.stream()
                .map(CartItem::getComboId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        List<String> cartItemIds = cartItems.stream().map(CartItem::getId).collect(Collectors.toList());

        Map<String, MenuItem> menuItemsById = menuItemIds.isEmpty()
                ? Collections.emptyMap()
                : menuItemRepository.findAllById(menuItemIds).stream()
                        .collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        Map<String, Combo> combosById = comboIds.isEmpty()
                ? Collections.emptyMap()
                : comboRepository.findAllById(comboIds).stream()
                        .collect(Collectors.toMap(Combo::getId, Function.identity()));
        Map<String, List<CartItemOption>> optionsByCartItem = cartItemIds.isEmpty()
                ? Collections.emptyMap()
                : cartItemOptionRepository.findByCartItemIdIn(cartItemIds).stream()
                        .collect(Collectors.groupingBy(opt -> opt.getCartItem().getId()));

        Set<String> optionItemIds = optionsByCartItem.values().stream()
                .flatMap(List::stream)
                .map(CartItemOption::getOptionItemId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, OptionItem> optionItemsById = optionItemIds.isEmpty()
                ? Collections.emptyMap()
                : optionItemRepository.findAllById(optionItemIds).stream()
                        .collect(Collectors.toMap(OptionItem::getId, Function.identity()));

        Set<String> optionGroupIds = optionItemsById.values().stream()
                .map(OptionItem::getOptionGroupId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        Map<String, OptionGroup> optionGroupsById = optionGroupIds.isEmpty()
                ? Collections.emptyMap()
                : optionGroupRepository.findAllById(optionGroupIds).stream()
                        .collect(Collectors.toMap(OptionGroup::getId, Function.identity()));

        for (CartItem cartItem : cartItems) {
            // Resolve real name + image instead of the placeholder
            String itemName;
            String itemImageUrl = null;
            if (StringUtils.hasText(cartItem.getMenuItemId())) {
                MenuItem mi = menuItemsById.get(cartItem.getMenuItemId());
                itemName = mi != null ? mi.getName() : ("Menu Item #" + cartItem.getMenuItemId());
                itemImageUrl = mi != null ? mi.getImageUrl() : null;
            } else if (StringUtils.hasText(cartItem.getComboId())) {
                Combo combo = combosById.get(cartItem.getComboId());
                itemName = combo != null ? combo.getName() : ("Combo #" + cartItem.getComboId());
                itemImageUrl = combo != null ? combo.getImageUrl() : null;
            } else {
                itemName = "Unknown item";
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItemId(cartItem.getMenuItemId());
            orderItem.setComboId(cartItem.getComboId());
            orderItem.setItemName(itemName);
            orderItem.setItemImageUrl(itemImageUrl);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getUnitPrice());
            orderItem.setTotalPrice(cartItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItem.setSpecialInstructions(cartItem.getSpecialInstructions());

            // Build the options_snapshot JSON now so we can store it on the order_item itself
            List<CartItemOption> cartOptions = optionsByCartItem.getOrDefault(cartItem.getId(), Collections.emptyList());
            List<SnapshotOption> snapshotOptions = new ArrayList<>(cartOptions.size());
            for (CartItemOption co : cartOptions) {
                OptionItem oi = optionItemsById.get(co.getOptionItemId());
                String optionName = oi != null ? oi.getName() : null;
                String groupName = null;
                if (oi != null && StringUtils.hasText(oi.getOptionGroupId())) {
                    OptionGroup og = optionGroupsById.get(oi.getOptionGroupId());
                    if (og != null) groupName = og.getName();
                }
                snapshotOptions.add(new SnapshotOption(
                        co.getOptionItemId(),
                        groupName,
                        optionName != null ? optionName : "",
                        co.getPriceAdjustment() != null ? co.getPriceAdjustment() : BigDecimal.ZERO
                ));
            }
            orderItem.setOptionsSnapshot(buildOptionsSnapshotJson(snapshotOptions));
            // saveAndFlush ensures the order_item row exists in the DB before the
            // dependent order_item_options INSERTs (FK constraint).
            OrderItem savedItem = orderItemRepository.saveAndFlush(orderItem);

            // Persist normalized order_item_options rows for reporting/historical queries
            if (!snapshotOptions.isEmpty()) {
                List<OrderItemOption> rows = new ArrayList<>(snapshotOptions.size());
                for (SnapshotOption snap : snapshotOptions) {
                    OrderItemOption row = new OrderItemOption();
                    row.setOrderItem(savedItem);
                    row.setOptionItemId(snap.optionItemId);
                    row.setOptionGroupName(snap.groupName);
                    row.setOptionName(StringUtils.hasText(snap.optionName) ? snap.optionName : "—");
                    row.setPriceAdjustment(snap.priceAdjustment);
                    rows.add(row);
                }
                orderItemOptionRepository.saveAll(rows);
            }
        }
    }

    private String buildOptionsSnapshotJson(List<SnapshotOption> options) {
        if (options == null || options.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < options.size(); i++) {
            SnapshotOption o = options.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"optionItemId\":").append(jsonString(o.optionItemId)).append(",")
              .append("\"groupName\":").append(jsonString(o.groupName)).append(",")
              .append("\"optionName\":").append(jsonString(o.optionName)).append(",")
              .append("\"priceAdjustment\":").append(o.priceAdjustment != null ? o.priceAdjustment.toPlainString() : "0")
              .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String jsonString(String value) {
        if (value == null) return "null";
        return "\"" + escapeJson(value) + "\"";
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomPart = String.format("%03d", RANDOM.nextInt(1000));
        return "FD-" + datePart + "-" + randomPart;
    }

    private String generatePickupCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(PICKUP_CODE_ALPHABET.charAt(RANDOM.nextInt(PICKUP_CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    private String generateDeliveryOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /**
     * Great-circle distance using the Haversine formula.
     * Returns {@code null} when either coordinate is missing so callers can decide what to do.
     */
    private BigDecimal haversineKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return null;
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadiusKm * c).setScale(2, java.math.RoundingMode.HALF_UP);
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


    private Map<String, Object> buildMerchantNotification(Order order, Store store) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", order.getId());
        payload.put("orderNumber", order.getOrderNumber());
        payload.put("storeId", order.getStoreId());
        payload.put("status", order.getStatus());
        payload.put("paymentMethod", order.getPaymentMethod());
        payload.put("paymentStatus", order.getPaymentStatus());
        payload.put("subtotal", order.getSubtotal());
        payload.put("totalAmount", order.getTotalAmount());
        payload.put("placedAt", order.getPlacedAt());
        payload.put("createdAt", order.getCreatedAt());
        payload.put("storeName", store != null ? store.getName() : null);
        return payload;
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

    /** Internal value object used while building the snapshot JSON + DB rows. */
    private record SnapshotOption(
            String optionItemId,
            String groupName,
            String optionName,
            BigDecimal priceAdjustment
    ) {}

    /** Result returned from {@link #reorderFromOrder(String, String)}. */
    public record ReorderResult(
            String cartId,
            String storeId,
            int copiedItems,
            int skippedItems
    ) {}
}
