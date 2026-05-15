package com.db.foodara.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreTagRequest {
    @NotBlank(message = "Tag name is required")
    private String name;
    private String slug;
    private String tagType;
    private String iconUrl;
    private String colorHex;
    private Integer displayOrder;
    private Boolean isActive;
}
