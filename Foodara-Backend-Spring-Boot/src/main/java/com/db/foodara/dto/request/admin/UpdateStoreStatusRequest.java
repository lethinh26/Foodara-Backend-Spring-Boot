package com.db.foodara.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStoreStatusRequest {
    @NotNull(message = "isActive is required")
    private Boolean isActive;
}
