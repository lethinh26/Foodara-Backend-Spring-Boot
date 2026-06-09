package com.db.foodara.notification.service;

import com.db.foodara.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Real-time WebSocket push for specific topics.
 * Handles order-specific and merchant-specific channels.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Push new order notification to merchant store topic.
     */
    public void sendNewOrderToMerchant(String storeId, Object payload) {
        String topic = "/topic/merchant." + storeId + ".orders";
        log.info("Pushing new order to merchant: {}", topic);
        messagingTemplate.convertAndSend(topic, payload);
    }

    /**
     * Push order status update to customer tracking topic.
     */
    public void sendOrderStatusToCustomer(String orderId, Object payload) {
        String topic = "/topic/orders." + orderId;
        log.info("Pushing order status to customer: {}", topic);
        messagingTemplate.convertAndSend(topic, payload);
    }

    /**
     * Push a generic notification to a specific user.
     */
    public void sendToUser(String userId, Notification notification) {
        String topic = "/topic/notifications." + userId;
        log.info("Pushing notification to user: {}", topic);
        messagingTemplate.convertAndSend(topic, notification);
    }
}
