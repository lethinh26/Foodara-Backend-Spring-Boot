package com.db.foodara.notification.channel;

import com.db.foodara.notification.entity.Notification;

/**
 * Strategy for dispatching a notification through a specific channel.
 */
public interface NotificationChannel {
    String name();
    void send(Notification notification);
}
