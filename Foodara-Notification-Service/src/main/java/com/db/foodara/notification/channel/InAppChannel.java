package com.db.foodara.notification.channel;

import com.db.foodara.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * In-app notification via WebSocket STOMP.
 * Subscribers listen on /topic/notifications.{userId}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppChannel implements NotificationChannel {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public String name() {
        return "in_app";
    }

    @Override
    public void send(Notification notification) {
        String destination = "/topic/notifications." + notification.getUserId();
        log.info("[InApp] Pushing notification {} to {}", notification.getId(), destination);
        messagingTemplate.convertAndSend(destination, notification);
    }
}
