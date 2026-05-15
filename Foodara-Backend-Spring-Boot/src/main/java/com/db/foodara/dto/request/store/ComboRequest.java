package com.db.foodara.dto.request.store;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ComboRequest {

    @NotBlank(message = "Combo name is required")
    private String name;

    private String description;

    @NotNull(message = "Combo price is required")
    @DecimalMin(value = "0.0")
    private BigDecimal comboPrice;

    private BigDecimal originalPrice;

    private Boolean isActive;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

}
