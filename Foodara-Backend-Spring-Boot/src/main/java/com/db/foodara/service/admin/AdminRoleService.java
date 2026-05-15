package com.db.foodara.service.admin;

import com.db.foodara.dto.request.admin.CreateRoleRequest;
import com.db.foodara.dto.request.admin.UpdateRolePermissionsRequest;
import com.db.foodara.dto.response.admin.PermissionResponse;
import com.db.foodara.dto.response.admin.RoleWithPermissionsResponse;
import com.db.foodara.entity.role.Permission;
import com.db.foodara.entity.role.Role;
import com.db.foodara.entity.role.RolePermission;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.role.PermissionRepository;
import com.db.foodara.repository.role.RolePermissionRepository;
import com.db.foodara.repository.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public List<RoleWithPermissionsResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RolePermission> allRolePermissions = rolePermissionRepository.findAllWithPermissions();

        // Group role_permissions by role_id
        var rpMap = allRolePermissions.stream()
                .collect(java.util.stream.Collectors.groupingBy(RolePermission::getRoleId));

        return roles.stream()
                .map(role -> {
                    List<RolePermission> rps = rpMap.getOrDefault(role.getId(), java.util.Collections.emptyList());
                    List<PermissionResponse> permissions = rps.stream()
                            .map(rp -> mapPermissionToResponse(rp.getPermission()))
                            .filter(p -> p != null)
                            .toList();

                    return RoleWithPermissionsResponse.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .description(role.getDescription())
                            .createdAt(role.getCreatedAt())
                            .permissions(permissions)
                            .build();
                })
                .toList();
    }

    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapPermissionToResponse)
                .toList();
    }

    @Transactional
    public RoleWithPermissionsResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.INVALID_KEY); // Or ROLE_EXISTS
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role = roleRepository.save(role);

        return mapRoleToResponse(role);
    }

    @Transactional
    public void updateRolePermissions(String roleId, UpdateRolePermissionsRequest request) {
        if (!roleRepository.existsById(roleId)) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        // Xóa permissions cũ
        rolePermissionRepository.deleteByRoleId(roleId);

        // Thêm permissions mới
        for (String permissionId : request.getPermissionIds()) {
            if (!permissionRepository.existsById(permissionId)) {
                throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
            }
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rolePermissionRepository.save(rp);
        }
    }

    @Transactional
    public void deleteRole(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Kiểm tra xem có phải core roles không
        if (role.getName().equalsIgnoreCase("admin") || role.getName().equalsIgnoreCase("superadmin")) {
            throw new AppException(ErrorCode.ADMIN_ACCESS_DENIED); // Cannot delete system roles
        }

        roleRepository.delete(role);
    }

    // --- Helpers ---

    private RoleWithPermissionsResponse mapRoleToResponse(Role role) {
        List<PermissionResponse> permissions = rolePermissionRepository.findByRoleIdWithPermissions(role.getId()).stream()
                .map(rp -> mapPermissionToResponse(rp.getPermission()))
                .filter(p -> p != null)
                .toList();

        return RoleWithPermissionsResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .permissions(permissions)
                .build();
    }

    private PermissionResponse mapPermissionToResponse(Permission p) {
        if (p == null) return null;
        return PermissionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .module(p.getModule())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
