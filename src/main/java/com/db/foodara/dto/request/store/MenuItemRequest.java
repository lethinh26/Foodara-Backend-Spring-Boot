package com.db.foodara.dto.request.store;

import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
}
