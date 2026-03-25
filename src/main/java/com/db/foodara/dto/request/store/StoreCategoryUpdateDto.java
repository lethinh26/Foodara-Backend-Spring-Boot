package com.db.foodara.dto.request.store;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreCategoryUpdateDto {
    private String name;
    private String slug;
    private String iconUrl;
    private Integer displayOrder;
    private Boolean isActive;
}
