package com.db.foodara.dto.request.store;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreCategoryUpdateDto {
    private String name;
    
    private String slug;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("display_order")
    private Integer displayOrder;

    @JsonProperty("is_active")
    private Boolean isActive;
}
