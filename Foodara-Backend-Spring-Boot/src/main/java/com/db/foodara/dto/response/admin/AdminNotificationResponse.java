package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminNotificationResponse {
    private String id;
    private String userId;
    private String userName;
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
