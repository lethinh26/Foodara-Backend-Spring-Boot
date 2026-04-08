package com.db.foodara.dto.request.store;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreCategoryCreateDto {
    @Size(min = 3, message = "Name must be at least 3 character")
    private String name;

    private String slug;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("is_active")
    private Boolean isActive;
}