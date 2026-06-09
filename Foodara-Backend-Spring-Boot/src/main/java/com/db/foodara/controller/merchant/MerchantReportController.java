package com.db.foodara.controller.merchant;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.merchant.MerchantRevenuePoint;
import com.db.foodara.service.merchant.MerchantReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * M09 — Merchant Reports endpoints.
 * All paths are scoped to a single store owned by the authenticated merchant.
 */
@RestController
@RequestMapping("/v1/merchant/{storeId}/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantReportController {

    private final MerchantReportService merchantReportService;

    @GetMapping("/total-revenue")
    public ApiResponse<BigDecimal> totalRevenue(Authentication authentication, @PathVariable String storeId) {
        return ApiResponse.success(merchantReportService.totalRevenue(authentication.getName(), storeId));
    }

    @GetMapping("/total-order")
    public ApiResponse<Long> totalOrder(Authentication authentication, @PathVariable String storeId) {
        return ApiResponse.success(merchantReportService.totalOrders(authentication.getName(), storeId));
    }

    @GetMapping("/avg-time")
    public ApiResponse<Integer> avgTime(Authentication authentication, @PathVariable String storeId) {
        return ApiResponse.success(merchantReportService.avgPreparationMinutes(authentication.getName(), storeId));
    }

    @GetMapping("/success-rate")
    public ApiResponse<BigDecimal> successRate(Authentication authentication, @PathVariable String storeId) {
        return ApiResponse.success(merchantReportService.successRatePercent(authentication.getName(), storeId));
    }

    @GetMapping("/revenue-data")
    public ApiResponse<List<MerchantRevenuePoint>> revenueData(
            Authentication authentication,
            @PathVariable String storeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ApiResponse.success(
                merchantReportService.revenueData(authentication.getName(), storeId, startDate, endDate));
    }
}
