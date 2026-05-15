package com.db.foodara.dto.request.admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignRolesRequest {
    @NotEmpty(message = "Role IDs are required")
    private List<String> roleIds;
}
