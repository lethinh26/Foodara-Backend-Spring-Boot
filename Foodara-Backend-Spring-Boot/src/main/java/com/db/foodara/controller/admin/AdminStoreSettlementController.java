package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.StoreSettlementItemResponse;
import com.db.foodara.dto.response.admin.StoreSettlementResponse;
import com.db.foodara.service.admin.AdminSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/store-settlements")
@RequiredArgsConstructor
public class AdminStoreSettlementController {

    private final AdminSettlementService adminSettlementService;

    @GetMapping
    public ApiResponse<PageResponse<StoreSettlementResponse>> getStoreSettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminSettlementService.getStoreSettlements(page, size, status));
    }

    @GetMapping("/{id}/items")
    public ApiResponse<List<StoreSettlementItemResponse>> getStoreSettlementItems(@PathVariable String id) {
        return ApiResponse.success(adminSettlementService.getStoreSettlementItems(id));
    }

    @PostMapping
    public ApiResponse<StoreSettlementResponse> generateStoreSettlement(@RequestBody Map<String, Object> request) {
        String adminUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(adminSettlementService.generateStoreSettlement(request, adminUserId));
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<Void> confirmStoreSettlement(@PathVariable String id) {
        String adminUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        adminSettlementService.confirmStoreSettlement(id, adminUserId);
        return ApiResponse.success("Store settlement confirmed");
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<Void> payStoreSettlement(@PathVariable String id, @RequestBody(required = false) Map<String, String> request) {
        adminSettlementService.payStoreSettlement(id, request);
        return ApiResponse.success("Store settlement marked as paid");
    }
}
