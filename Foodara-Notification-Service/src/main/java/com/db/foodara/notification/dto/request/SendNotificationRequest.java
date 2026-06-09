package com.db.foodara.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendNotificationRequest {
    @NotBlank
    private String targetType; // all, role, user

    private String targetValue; // role name or user id

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    private String imageUrl;

    @NotBlank
    private String notificationType; // system, promo, order, payment

    private String referenceType;

    private String referenceId;

    @NotBlank
    private String channel; // in_app, email, sms, push
}
