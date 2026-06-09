package com.db.foodara.notification.service;

import com.db.foodara.notification.channel.NotificationChannel;
import com.db.foodara.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Routes a notification to the appropriate channel(s).
 * Channels are specified as comma-separated (e.g. "in_app,email").
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final List<NotificationChannel> channels;

    public void dispatch(Notification notification) {
        if (notification.getChannel() == null || notification.getChannel().isBlank()) {
            log.warn("No channel specified for notification {}", notification.getId());
            return;
        }

        String[] channelNames = notification.getChannel().split(",");
        for (String ch : channelNames) {
            String trimmed = ch.trim().toLowerCase();
            channels.stream()
                    .filter(c -> c.name().equals(trimmed))
                    .findFirst()
                    .ifPresentOrElse(
                            c -> {
                                try {
                                    c.send(notification);
                                    log.info("Dispatched notification {} via {}", notification.getId(), trimmed);
                                } catch (Exception e) {
                                    log.error("Failed to dispatch notification {} via {}: {}",
                                            notification.getId(), trimmed, e.getMessage());
                                }
                            },
                            () -> log.warn("Unknown channel '{}' for notification {}", trimmed, notification.getId())
                    );
        }
    }
}
