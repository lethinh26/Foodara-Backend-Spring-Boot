package com.db.foodara.controller.admin;

import com.db.foodara.dto.request.admin.AssignRolesRequest;
import com.db.foodara.dto.request.admin.UpdateUserStatusRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminSessionResponse;
import com.db.foodara.dto.response.admin.AdminUserResponse;
import com.db.foodara.service.admin.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResponse<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role) {
        return ApiResponse.success(adminUserService.getUsers(page, size, search, status, role));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserResponse> getUserDetail(@PathVariable String id) {
        return ApiResponse.success(adminUserService.getUserDetail(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable String id,
                                               @RequestBody @Valid UpdateUserStatusRequest request) {
        String adminUserId = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        adminUserService.updateUserStatus(id, request, adminUserId);
        return ApiResponse.success("User status updated");
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<Void> assignRoles(@PathVariable String id,
                                          @RequestBody @Valid AssignRolesRequest request) {
        adminUserService.assignRoles(id, request);
        return ApiResponse.success("Roles assigned");
    }

    @GetMapping("/{id}/sessions")
    public ApiResponse<List<AdminSessionResponse>> getUserSessions(@PathVariable String id) {
        return ApiResponse.success(adminUserService.getUserSessions(id));
    }

    @DeleteMapping("/{id}/sessions/{sessionId}")
    public ApiResponse<Void> revokeSession(@PathVariable String id,
                                            @PathVariable String sessionId) {
        adminUserService.revokeSession(id, sessionId);
        return ApiResponse.success("Session revoked");
    }
}
