package com.db.foodara.service.order;

import com.db.foodara.dto.request.order.RejectOrderRequest;
import com.db.foodara.entity.order.Order;
import com.db.foodara.entity.order.OrderStatusHistory;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.repository.order.OrderAssignmentRepository;
import com.db.foodara.repository.order.OrderItemRepository;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.order.OrderStatusHistoryRepository;
import com.db.foodara.repository.store.StoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
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

    //114	GET	/api/merchant/stores/:storeId/orders	Danh sách đơn hàng
    public List<Order> getOrders(String userId, String storeId){
        merchantRepository.findByOwnerId(userId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        storeRepository.findStoreById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return orderRepository.findAll();
    }

    //115	GET	/api/merchant/orders/:id	Chi tiết đơn
    public Order getOrderDetail(String userId, String storeId, String orderId) {
        return validateAndGetOrder(userId, storeId, orderId);
    }

    //116	PUT	/api/merchant/orders/:id/accept	Chấp nhận đơn
    @Transactional
    public Order acceptOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);

        String oldStatus = order.getStatus();
        order.setStatus("CONFIRMED");
        order.setConfirmedAt(LocalDateTime.now());

        saveStatusHistory(order, oldStatus, "CONFIRMED", userId, "Merchant accepted order");
        return orderRepository.save(order);
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
        order.setStatus("READY_FOR_PICKUP");
        order.setReadyAt(LocalDateTime.now());

        saveStatusHistory(order, oldStatus, "READY_FOR_PICKUP", userId, "Food is ready for driver");
        return orderRepository.save(order);
    }

    //120	PUT	/api/merchant/orders/:id/handover	Xác nhận giao cho tài xế
    @Transactional
    public Order handoverOrder(String userId, String storeId, String orderId) {
        Order order = validateAndGetOrder(userId, storeId, orderId);

        String oldStatus = order.getStatus();
        order.setStatus("PICKED_UP");
        order.setPickedUpAt(LocalDateTime.now());

        saveStatusHistory(order, oldStatus, "PICKED_UP", userId, "Handed over to driver");
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
}
