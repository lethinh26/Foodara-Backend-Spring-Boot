package com.db.foodara.dto.request.store;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class MenuItemRequest {
    @NotBlank(message = "Store ID not empty")
    private String storeId;

    private String categoryId;

    @Size(max = 255)
    private String name;

    private String description;

    private String imageUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price is invalid")
    private BigDecimal basePrice;

    private Boolean isAvailable;

    private Boolean isActive;

    private Boolean trackInventory;

    @Min(value = 0, message = "Amount of item invalid")
    private Integer stockQuantity;

    @Min(value = 1, message = "Amount of item invalid")
    private Integer maxQuantityPerOrder;

    private Integer dailyLimit;

    private Boolean isPopular;

    private Boolean isNew;

    private Integer displayOrder;

    /**
     * IDs of option groups (toppings, sizes, ...) attached to this menu item.
     * Stored in {@code menu_item_option_groups} as a many-to-many bridge.
     * If {@code null}, the existing assignments are left unchanged on update;
     * if empty, all assignments are cleared.
     */
    private java.util.List<String> optionGroupIds;
}
