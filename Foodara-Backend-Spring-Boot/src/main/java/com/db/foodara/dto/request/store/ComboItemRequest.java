package com.db.foodara.dto.request.store;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComboItemRequest {
    @NotBlank(message = "MenuItem ID is required")
    private String menuItemId;

    @Min(value = 1, message = "Number of item is invalid")
    private Integer quantity;
}