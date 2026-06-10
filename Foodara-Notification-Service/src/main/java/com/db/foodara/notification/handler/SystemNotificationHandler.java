package com.db.foodara.notification.handler;

import com.db.foodara.notification.entity.Notification;
import com.db.foodara.notification.repository.NotificationRepository;
import com.db.foodara.notification.service.NotificationService;
import com.db.foodara.notification.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handles system broadcast notifications (promo, maintenance, etc.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemNotificationHandler {

    private final NotificationService notificationService;
    private final WebSocketNotificationService webSocketService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = "#{systemNotificationQueue.name}")
    public void handleSystemNotify(Map<String, Object> event) {
        log.info("Received SystemNotificationEvent: type={}", event.get("notificationType"));

        String title = (String) event.get("title");
        String body = (String) event.get("body");
        String channel = (String) event.getOrDefault("channel", "in_app");
        String notificationType = (String) event.getOrDefault("notificationType", "promotion");

        if (title == null || body == null) {
            log.warn("SystemNotificationEvent missing title/body, skip");
            return;
        }

        // Broadcast to specific user if userId provided
        String userId = (String) event.get("userId");
        if (userId != null && !userId.isBlank()) {
            sendToUser(userId, title, body, notificationType, channel, event);
            return;
        }

        // Broadcast to all users via WebSocket
        webSocketService.sendBroadcast(notificationType, Map.of(
                "title", title,
                "body", body,
                "notificationType", notificationType
        ));

        log.info("Broadcast promo notification: {}", title);
    }

    private void sendToUser(String userId, String title, String body, String type, String channel, Map<String, Object> event) {
        String email = (String) event.get("recipientEmail");

        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setBody(body);
        n.setNotificationType(type);
        n.setChannel(channel);
        n.setRecipientEmail(email);
        n.setSentAt(LocalDateTime.now());
        notificationService.createAndSend(n);
    }
}
