package com.db.foodara.dto.request.order;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateCartItemRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private List<String> optionItemIds;

    private String specialInstructions;
}
