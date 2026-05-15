package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.DriverSettlementResponse;
import com.db.foodara.service.admin.AdminSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/admin/driver-settlements")
@RequiredArgsConstructor
public class AdminDriverSettlementController {

    private final AdminSettlementService adminSettlementService;

    @GetMapping
    public ApiResponse<PageResponse<DriverSettlementResponse>> getDriverSettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminSettlementService.getDriverSettlements(page, size, status));
    }

    @PostMapping
    public ApiResponse<DriverSettlementResponse> generateDriverSettlement(@RequestBody Map<String, Object> request) {
        String adminUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(adminSettlementService.generateDriverSettlement(request, adminUserId));
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<Void> confirmDriverSettlement(@PathVariable String id) {
        String adminUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        adminSettlementService.confirmDriverSettlement(id, adminUserId);
        return ApiResponse.success("Driver settlement confirmed");
    }
}
