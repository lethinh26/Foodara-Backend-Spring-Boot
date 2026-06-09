package com.db.foodara.notification.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationResponse {
    private String id;
    private String userId;
    private String title;
    private String body;
    private String imageUrl;
    private String notificationType;
    private String referenceType;
    private String referenceId;
    private String channel;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime sentAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
