package com.db.foodara.service.admin;

import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminAuditLogResponse;
import com.db.foodara.entity.audit.AdminAuditLog;
import com.db.foodara.entity.user.User;
import com.db.foodara.repository.audit.AdminAuditLogRepository;
import com.db.foodara.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuditService {

    private final AdminAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public PageResponse<AdminAuditLogResponse> getLogs(int page, int size, String module, String adminId, String action) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminAuditLog> logPage;

        if (module != null && !module.isBlank()) {
            logPage = auditLogRepository.findByModule(module, pageRequest);
        } else if (adminId != null && !adminId.isBlank()) {
            logPage = auditLogRepository.findByAdminId(adminId, pageRequest);
        } else if (action != null && !action.isBlank()) {
            logPage = auditLogRepository.findByAction(action, pageRequest);
        } else {
            logPage = auditLogRepository.findAll(pageRequest);
        }

        List<AdminAuditLogResponse> content = logPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AdminAuditLogResponse>builder()
                .content(content)
                .page(logPage.getNumber())
                .number(logPage.getNumber())
                .size(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .last(logPage.isLast())
                .build();
    }

    public void logAction(String adminId, String action, String module, String entityType, String entityId,
                          String oldValues, String newValues) {
        AdminAuditLog auditLog = new AdminAuditLog();
        auditLog.setAdminId(adminId);
        auditLog.setAction(action);
        auditLog.setModule(module);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLogRepository.save(auditLog);
    }

    private AdminAuditLogResponse mapToResponse(AdminAuditLog l) {
        String adminName = userRepository.findById(l.getAdminId())
                .map(User::getFullName)
                .orElse(null);

        return AdminAuditLogResponse.builder()
                .id(l.getId())
                .adminId(l.getAdminId())
                .adminName(adminName)
                .action(l.getAction())
                .module(l.getModule())
                .entityType(l.getEntityType())
                .entityId(l.getEntityId())
                .oldValues(l.getOldValues())
                .newValues(l.getNewValues())
                .ipAddress(l.getIpAddress())
                .userAgent(l.getUserAgent())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
