package com.db.foodara.controller.admin;

import com.db.foodara.dto.request.admin.CreateRoleRequest;
import com.db.foodara.dto.request.admin.UpdateRolePermissionsRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.admin.PermissionResponse;
import com.db.foodara.dto.response.admin.RoleWithPermissionsResponse;
import com.db.foodara.service.admin.AdminRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    // --- Roles ---

    @GetMapping("/roles")
    public ApiResponse<List<RoleWithPermissionsResponse>> getAllRoles() {
        return ApiResponse.success(adminRoleService.getAllRoles());
    }

    @PostMapping("/roles")
    public ApiResponse<RoleWithPermissionsResponse> createRole(@RequestBody @Valid CreateRoleRequest request) {
        return ApiResponse.success(adminRoleService.createRole(request));
    }

    @PutMapping("/roles/{id}/permissions")
    public ApiResponse<Void> updateRolePermissions(@PathVariable String id,
                                                   @RequestBody @Valid UpdateRolePermissionsRequest request) {
        adminRoleService.updateRolePermissions(id, request);
        return ApiResponse.success("Role permissions updated");
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable String id) {
        adminRoleService.deleteRole(id);
        return ApiResponse.success("Role deleted");
    }

    // --- Permissions ---

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> getAllPermissions() {
        return ApiResponse.success(adminRoleService.getAllPermissions());
    }
}
