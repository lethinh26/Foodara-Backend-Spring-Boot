package com.db.foodara.dto.request.store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComboRequest {

    @NotBlank(message = "Combo name is required")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "Combo price is required")
    @DecimalMin(value = "0.0")
    private BigDecimal comboPrice;

    private BigDecimal originalPrice;

    private Boolean isActive;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

}
