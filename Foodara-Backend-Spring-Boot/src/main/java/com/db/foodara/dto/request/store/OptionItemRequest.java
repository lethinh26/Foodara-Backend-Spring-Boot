package com.db.foodara.dto.request.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OptionItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 255)
    private String name;
    private BigDecimal priceAdjustment;

    private Boolean isAvailable;

    private Boolean isDefault;

    private Integer displayOrder;
}