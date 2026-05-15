package com.db.foodara.service.admin;

import com.db.foodara.dto.request.admin.AssignRolesRequest;
import com.db.foodara.dto.request.admin.UpdateUserStatusRequest;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminSessionResponse;
import com.db.foodara.dto.response.admin.AdminUserResponse;
import com.db.foodara.entity.role.Role;
import com.db.foodara.entity.user.User;
import com.db.foodara.entity.user.UserRole;
import com.db.foodara.entity.user.UserSession;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.role.RoleRepository;
import com.db.foodara.repository.user.UserRepository;
import com.db.foodara.repository.user.UserRoleRepository;
import com.db.foodara.repository.user.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private static final Set<String> VALID_STATUSES = Set.of("active", "suspended", "banned");

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserSessionRepository userSessionRepository;
    private final RoleRepository roleRepository;

    // Danh sách user có phân trang + tìm kiếm
    public PageResponse<AdminUserResponse> getUsers(int page, int size, String search, String status, String role) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage;

        if (search != null && !search.isBlank()) {
            userPage = userRepository.searchUsers(search.trim(), pageRequest);
        } else if (status != null && !status.isBlank()) {
            userPage = userRepository.findByStatus(status, pageRequest);
        } else {
            userPage = userRepository.findAll(pageRequest);
        }

        List<AdminUserResponse> content = userPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AdminUserResponse>builder()
                .content(content)
                .page(userPage.getNumber())
                .number(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    // Chi tiết user
    public AdminUserResponse getUserDetail(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return mapToResponse(user);
    }

    // Cập nhật trạng thái user
    @Transactional
    public void updateUserStatus(String userId, UpdateUserStatusRequest request, String adminUserId) {
        if (!VALID_STATUSES.contains(request.getStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Guard: không tự thay đổi trạng thái chính mình
        if (userId.equals(adminUserId)) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_SELF);
        }

        // Guard: không thay đổi trạng thái superadmin
        List<UserRole> targetRoles = userRoleRepository.findByUserId(userId);
        boolean isSuperAdmin = targetRoles.stream()
                .anyMatch(ur -> ur.getRole() != null && "superadmin".equals(ur.getRole().getName()));
        if (isSuperAdmin) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_SUPERADMIN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(request.getStatus());
        userRepository.save(user);

        // Khi ban/suspend → revoke tất cả sessions active để force logout
        if ("banned".equals(request.getStatus()) || "suspended".equals(request.getStatus())) {
            List<UserSession> activeSessions = userSessionRepository.findByUserId(userId).stream()
                    .filter(s -> s.getRevokedAt() == null)
                    .toList();
            for (UserSession session : activeSessions) {
                session.setRevokedAt(LocalDateTime.now());
                userSessionRepository.save(session);
            }
            log.info("Admin {} revoked {} sessions for user {}", adminUserId, activeSessions.size(), userId);
        }

        log.info("Admin {} updated user {} status to {}", adminUserId, userId, request.getStatus());
    }

    // Gán roles cho user
    @Transactional
    public void assignRoles(String userId, AssignRolesRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // Xóa roles cũ rồi gán mới
        List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
        userRoleRepository.deleteAll(existingRoles);

        for (String roleId : request.getRoleIds()) {
            if (!roleRepository.existsById(roleId)) {
                throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            }
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleRepository.save(ur);
        }
    }

    // Danh sách sessions
    public List<AdminSessionResponse> getUserSessions(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return userSessionRepository.findByUserId(userId).stream()
                .map(this::mapSessionToResponse)
                .toList();
    }

    // Thu hồi session
    @Transactional
    public void revokeSession(String userId, String sessionId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
        if (!session.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.SESSION_NOT_FOUND);
        }
        session.setRevokedAt(LocalDateTime.now());
        userSessionRepository.save(session);
    }

    // --- Mapping helpers ---

    private AdminUserResponse mapToResponse(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        List<AdminUserResponse.RoleInfo> roles = userRoles.stream()
                .map(ur -> {
                    Role role = ur.getRole();
                    if (role == null) return null;
                    return AdminUserResponse.RoleInfo.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .description(role.getDescription())
                            .createdAt(role.getCreatedAt())
                            .build();
                })
                .filter(r -> r != null)
                .toList();

        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roles)
                .build();
    }

    private AdminSessionResponse mapSessionToResponse(UserSession s) {
        return AdminSessionResponse.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .ipAddress(s.getIpAddress())
                .userAgent(s.getUserAgent())
                .expiresAt(s.getExpiresAt())
                .revokedAt(s.getRevokedAt())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
