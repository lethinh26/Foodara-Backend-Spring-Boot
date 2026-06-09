package com.db.foodara.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateRolePermissionsRequest {
    @NotNull(message = "Permissions list cannot be null")
    private List<String> permissionIds;
}
