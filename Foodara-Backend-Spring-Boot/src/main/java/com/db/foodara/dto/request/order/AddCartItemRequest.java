package com.db.foodara.dto.request.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AddCartItemRequest {

    @NotBlank(message = "Store ID is required")
    private String storeId;

    private String menuItemId;

    private String comboId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    private List<String> optionItemIds = new ArrayList<>();

    private String specialInstructions;
}
