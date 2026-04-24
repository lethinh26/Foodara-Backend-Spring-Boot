package com.db.foodara.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectOrderRequest {
    @NotBlank(message = "Reason is required")
    private String reason;
}