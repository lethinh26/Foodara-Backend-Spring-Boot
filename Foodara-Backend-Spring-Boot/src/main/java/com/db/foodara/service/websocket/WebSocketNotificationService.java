package com.db.foodara.service.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo có đơn hàng mới cho Merchant
     * @param storeId ID của quán
     * @param payload Dữ liệu đơn hàng mới
     */
    public void sendNewOrderToMerchant(String storeId, Object payload) {
        String topic = "/topic/merchant." + storeId + ".orders";
        log.info("Sending new order to merchant topic: {}", topic);
        messagingTemplate.convertAndSend(topic, payload);
    }

    /**
     * Gửi thông báo cập nhật trạng thái đơn hàng cho Customer
     * @param orderId ID của đơn hàng
     * @param payload Dữ liệu cập nhật
     */
    public void sendOrderStatusToCustomer(String orderId, Object payload) {
        String topic = "/topic/orders." + orderId;
        log.info("Sending order status update to customer topic: {}", topic);
        messagingTemplate.convertAndSend(topic, payload);
    }
}
