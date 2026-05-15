package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminVoucherResponse;
import com.db.foodara.service.admin.AdminVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final AdminVoucherService adminVoucherService;

    @GetMapping
    public ApiResponse<PageResponse<AdminVoucherResponse>> getVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isActive) {
        return ApiResponse.success(adminVoucherService.getVouchers(page, size, search, type, isActive));
    }

    @PostMapping
    public ApiResponse<AdminVoucherResponse> createVoucher(@RequestBody Map<String, Object> request) {
        return ApiResponse.success(adminVoucherService.createVoucher(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateVoucher(@PathVariable String id, @RequestBody Map<String, Object> request) {
        adminVoucherService.updateVoucher(id, request);
        return ApiResponse.success("Voucher updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVoucher(@PathVariable String id) {
        adminVoucherService.deleteVoucher(id);
        return ApiResponse.success("Voucher deleted");
    }
}
