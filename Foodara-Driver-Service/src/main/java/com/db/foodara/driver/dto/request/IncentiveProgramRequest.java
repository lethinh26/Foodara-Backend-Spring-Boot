package com.db.foodara.driver.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class IncentiveProgramRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Target type is required")
    private String targetType;

    @NotNull(message = "Target value is required")
    @Min(value = 1, message = "Target value must be at least 1")
    private Integer targetValue;

    @NotNull(message = "Bonus amount is required")
    @DecimalMin(value = "0.01", message = "Bonus amount must be greater than 0")
    private BigDecimal bonusAmount;

    private Integer maxParticipants;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    private Boolean isActive;
}
