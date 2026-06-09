package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminAuditLogResponse;
import com.db.foodara.service.admin.AdminAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    @GetMapping
    public ApiResponse<PageResponse<AdminAuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String adminId,
            @RequestParam(required = false) String action) {
        return ApiResponse.success(adminAuditService.getLogs(page, size, module, adminId, action));
    }
}
