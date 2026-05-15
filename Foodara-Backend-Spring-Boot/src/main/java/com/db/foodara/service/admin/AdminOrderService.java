package com.db.foodara.service.admin;

import com.db.foodara.dto.request.admin.AssignDriverRequest;
import com.db.foodara.dto.request.admin.UpdateOrderStatusRequest;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.*;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.order.OrderAssignment;
import com.db.foodara.entity.order.OrderItem;
import com.db.foodara.entity.order.OrderStatusHistory;
import com.db.foodara.entity.user.User;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.order.OrderAssignmentRepository;
import com.db.foodara.repository.order.OrderItemRepository;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.order.OrderStatusHistoryRepository;
import com.db.foodara.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    private static final Set<String> VALID_ORDER_STATUSES = Set.of(
            "pending", "confirmed", "preparing", "ready_for_pickup",
            "driver_assigned", "driver_at_store", "picked_up",
            "delivering", "delivered", "completed", "cancelled", "failed"
    );

    private static final Map<String, Set<String>> ADMIN_TRANSITIONS = Map.ofEntries(
            Map.entry("pending", Set.of("confirmed", "cancelled")),
            Map.entry("confirmed", Set.of("preparing", "cancelled")),
            Map.entry("preparing", Set.of("ready_for_pickup", "cancelled")),
            Map.entry("ready_for_pickup", Set.of("driver_assigned", "cancelled")),
            Map.entry("driver_assigned", Set.of("driver_at_store", "cancelled")),
            Map.entry("driver_at_store", Set.of("picked_up", "cancelled")),
            Map.entry("picked_up", Set.of("delivering", "cancelled")),
            Map.entry("delivering", Set.of("delivered", "failed")),
            Map.entry("delivered", Set.of("completed")),
            Map.entry("completed", Set.of()),
            Map.entry("cancelled", Set.of()),
            Map.entry("failed", Set.of())
    );


    public PageResponse<AdminOrderResponse> getOrders(int page, int size, String search, String status, String paymentStatus) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("placedAt").descending());
        Page<Order> orderPage;

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasPaymentStatus = paymentStatus != null && !paymentStatus.isBlank();

        if (hasSearch) {
            orderPage = orderRepository.searchOrders(search.trim(), pageRequest);
        } else if (hasStatus && hasPaymentStatus) {
            orderPage = orderRepository.findByStatusAndPaymentStatus(status, paymentStatus, pageRequest);
        } else if (hasStatus) {
            orderPage = orderRepository.findByStatus(status, pageRequest);
        } else if (hasPaymentStatus) {
            orderPage = orderRepository.findByPaymentStatus(paymentStatus, pageRequest);
        } else {
            orderPage = orderRepository.findAll(pageRequest);
        }

        List<AdminOrderResponse> content = orderPage.getContent().stream()
                .map(this::mapOrderToResponse)
                .toList();

        return PageResponse.<AdminOrderResponse>builder()
                .content(content)
                .page(orderPage.getNumber())
                .number(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }


    public AdminOrderResponse getOrderDetail(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return mapOrderToResponse(order);
    }


    public List<AdminOrderItemResponse> getOrderItems(String orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(this::mapOrderItemToResponse)
                .toList();
    }


    public List<OrderHistoryResponse> getOrderHistory(String orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        return statusHistoryRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::mapHistoryToResponse)
                .toList();
    }


    public List<OrderAssignmentResponse> getOrderAssignments(String orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        return assignmentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::mapAssignmentToResponse)
                .toList();
    }


    @Transactional
    public void updateOrderStatus(String orderId, UpdateOrderStatusRequest request, String adminUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String newStatus = request.getStatus();
        if (!VALID_ORDER_STATUSES.contains(newStatus)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        String currentStatus = order.getStatus();
        Set<String> allowedNext = ADMIN_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedNext.contains(newStatus)) {
            throw new AppException(ErrorCode.ORDER_CANNOT_CANCEL);
        }

        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        updateStatusTimestamp(order, newStatus);

        if ("cancelled".equals(newStatus)) {
            order.setCancelledAt(LocalDateTime.now());
            order.setCancelledBy("admin");
            order.setCancellationReason(request.getNote());
        }

        orderRepository.save(order);

        // Log to status history
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(oldStatus);
        history.setToStatus(newStatus);
        history.setChangedBy(adminUserId);
        history.setChangedByRole("admin");
        history.setNote(request.getNote());
        history.setCreatedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);

        log.info("Admin {} changed order {} status: {} -> {}", adminUserId, orderId, oldStatus, newStatus);
    }


    @Transactional
    public void assignDriver(String orderId, AssignDriverRequest request, String adminUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String driverId = request.getDriverId();
        // Validate driver exists in users table
        if (!userRepository.existsById(driverId)) {
            throw new AppException(ErrorCode.DRIVER_NOT_FOUND);
        }

        // Cancel any existing pending assignment
        assignmentRepository.findByOrderIdAndStatus(orderId, "proposed")
                .ifPresent(existing -> {
                    existing.setStatus("cancelled");
                    existing.setRespondedAt(LocalDateTime.now());
                    assignmentRepository.save(existing);
                });

        // Create new assignment
        OrderAssignment assignment = new OrderAssignment();
        assignment.setOrder(order);
        assignment.setDriverId(driverId);
        assignment.setAssignmentType("manual");
        assignment.setStatus("accepted");
        assignment.setProposedAt(LocalDateTime.now());
        assignment.setRespondedAt(LocalDateTime.now());
        assignment.setCreatedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        // Update order driver
        String oldStatus = order.getStatus();
        order.setDriverId(driverId);
        if ("ready_for_pickup".equals(order.getStatus()) || "pending".equals(order.getStatus())
                || "confirmed".equals(order.getStatus()) || "preparing".equals(order.getStatus())) {
            order.setStatus("driver_assigned");
        }
        orderRepository.save(order);

        // Log history
        if (!oldStatus.equals(order.getStatus())) {
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrder(order);
            history.setFromStatus(oldStatus);
            history.setToStatus(order.getStatus());
            history.setChangedBy(adminUserId);
            history.setChangedByRole("admin");
            history.setNote("Manual driver assignment by admin");
            history.setCreatedAt(LocalDateTime.now());
            statusHistoryRepository.save(history);
        }

        log.info("Admin {} assigned driver {} to order {}", adminUserId, driverId, orderId);
    }

    // ==================== MAPPERS ====================

    private AdminOrderResponse mapOrderToResponse(Order o) {
        // Enrich with customer info
        String customerName = null;
        String customerEmail = null;
        String customerPhone = null;
        if (o.getCustomerId() != null) {
            User customer = userRepository.findById(o.getCustomerId()).orElse(null);
            if (customer != null) {
                customerName = customer.getFullName();
                customerEmail = customer.getEmail();
                customerPhone = customer.getPhone();
            }
        }

        // Enrich with driver info
        String driverName = null;
        String driverPhone = null;
        if (o.getDriverId() != null) {
            User driver = userRepository.findById(o.getDriverId()).orElse(null);
            if (driver != null) {
                driverName = driver.getFullName();
                driverPhone = driver.getPhone();
            }
        }

        return AdminOrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .customerId(o.getCustomerId())
                .customerName(customerName)
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .storeId(o.getStoreId())
                .storeName(o.getStoreName())
                .storeAddress(o.getStoreAddress())
                .driverId(o.getDriverId())
                .driverName(driverName)
                .driverPhone(driverPhone)
                .status(o.getStatus())
                .subtotal(o.getSubtotal())
                .deliveryFee(o.getDeliveryFee())
                .deliveryFeeDiscount(o.getDeliveryFeeDiscount())
                .platformFee(o.getPlatformFee())
                .surgeFee(o.getSurgeFee())
                .storeDiscount(o.getStoreDiscount())
                .voucherDiscount(o.getVoucherDiscount())
                .totalAmount(o.getTotalAmount())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .deliveryNote(o.getDeliveryNote())
                .deliveryDistanceKm(o.getDeliveryDistanceKm())
                .estimatedPrepTime(o.getEstimatedPrepTime())
                .estimatedDeliveryTime(o.getEstimatedDeliveryTime())
                .estimatedTotalTime(o.getEstimatedTotalTime())
                .placedAt(o.getPlacedAt())
                .confirmedAt(o.getConfirmedAt())
                .preparingAt(o.getPreparingAt())
                .readyAt(o.getReadyAt())
                .pickedUpAt(o.getPickedUpAt())
                .deliveredAt(o.getDeliveredAt())
                .completedAt(o.getCompletedAt())
                .cancelledAt(o.getCancelledAt())
                .cancelledBy(o.getCancelledBy())
                .cancellationReason(o.getCancellationReason())
                .commissionRate(o.getCommissionRate())
                .commissionAmount(o.getCommissionAmount())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private AdminOrderItemResponse mapOrderItemToResponse(OrderItem item) {
        return AdminOrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .comboId(item.getComboId())
                .itemName(item.getItemName())
                .itemImageUrl(item.getItemImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .optionsSnapshot(item.getOptionsSnapshot())
                .specialInstructions(item.getSpecialInstructions())
                .build();
    }

    private OrderHistoryResponse mapHistoryToResponse(OrderStatusHistory h) {
        String changedByName = null;
        if (h.getChangedBy() != null) {
            changedByName = userRepository.findById(h.getChangedBy())
                    .map(User::getFullName)
                    .orElse(null);
        }

        return OrderHistoryResponse.builder()
                .id(h.getId())
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .changedBy(h.getChangedBy())
                .changedByRole(h.getChangedByRole())
                .changedByName(changedByName)
                .note(h.getNote())
                .createdAt(h.getCreatedAt())
                .build();
    }

    private OrderAssignmentResponse mapAssignmentToResponse(OrderAssignment a) {
        String driverName = null;
        if (a.getDriverId() != null) {
            driverName = userRepository.findById(a.getDriverId())
                    .map(User::getFullName)
                    .orElse(null);
        }

        return OrderAssignmentResponse.builder()
                .id(a.getId())
                .orderId(a.getOrder().getId())
                .driverId(a.getDriverId())
                .driverName(driverName)
                .assignmentType(a.getAssignmentType())
                .status(a.getStatus())
                .distanceToStoreKm(a.getDistanceToStoreKm())
                .responseDeadline(a.getResponseDeadline())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private void updateStatusTimestamp(Order order, String status) {
        switch (status) {
            case "confirmed" -> order.setConfirmedAt(LocalDateTime.now());
            case "preparing" -> order.setPreparingAt(LocalDateTime.now());
            case "ready_for_pickup" -> order.setReadyAt(LocalDateTime.now());
            case "picked_up" -> order.setPickedUpAt(LocalDateTime.now());
            case "delivered" -> order.setDeliveredAt(LocalDateTime.now());
            case "completed" -> order.setCompletedAt(LocalDateTime.now());
            default -> {} // No specific timestamp for other statuses
        }
    }
}
