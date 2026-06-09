package com.db.foodara.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTemplateRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String channel;

    private String subject;

    @NotBlank
    private String bodyTemplate;

    private String variables; // JSON array string
}
