package com.db.foodara.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    private String slug;
    private String iconUrl;
    private Integer displayOrder;
    private Boolean isActive;
}
