package com.db.foodara.dto.request.store;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreCategoryCreateDto {
    @Size(min = 1, message = "Name must be at least 1 character")
    private String name;
    private String slug;
    private String iconUrl;
    private Integer displayOrder;
    private Boolean isActive;
}