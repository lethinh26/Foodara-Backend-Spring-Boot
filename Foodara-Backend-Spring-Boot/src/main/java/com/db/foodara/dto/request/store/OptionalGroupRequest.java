package com.db.foodara.dto.request.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionalGroupRequest {
    @NotBlank(message = "Store id is required")
    private String storeId;
    @NotBlank(message = "Name of option id required")
    private String name;

    private Integer minSelections = 0;

    private Integer maxSelections = 1;

    private Integer displayOrder = 0;
}
