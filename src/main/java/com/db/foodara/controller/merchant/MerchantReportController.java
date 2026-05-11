package com.db.foodara.controller.merchant;

import com.db.foodara.dto.request.merchant.MerchantRegisterRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.order.DailyRevenueResponse;
import com.db.foodara.service.merchant.MerchantReportService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/merchant/{storeId}")
@RequiredArgsConstructor
public class MerchantReportController {
    private final MerchantReportService merchantReportService;

    @GetMapping("/reports/total-revenue")
    public ApiResponse<Double> getTotalRevenue(Authentication authentication,
                                               @PathVariable String storeId){
        return ApiResponse.success(merchantReportService.getAllRevenue(authentication.getName(), storeId));
    }

    @GetMapping("/reports/total-order")
    public ApiResponse<Integer> getTotalOrder(Authentication authentication,
                                               @PathVariable String storeId){
        return ApiResponse.success(merchantReportService.getTotalOrder(authentication.getName(), storeId));
    }

    @GetMapping("/reports/avg-time")
    public ApiResponse<Double> getAVGTime(Authentication authentication,
                                               @PathVariable String storeId){
        return ApiResponse.success(merchantReportService.getAVGTime(authentication.getName(), storeId));
    }

    @GetMapping("/reports/success-rate")
    public ApiResponse<Double> getSuccessOrderRate(Authentication authentication,
                                               @PathVariable String storeId){
        return ApiResponse.success(merchantReportService.getSuccessOrderRate(authentication.getName(), storeId));
    }

    @GetMapping("/reports/revenue-data")
    public ApiResponse<List<DailyRevenueResponse>> getRevenuData(
            Authentication authentication,
            @PathVariable String storeId,
            @RequestParam(name = "start", required = false)LocalDateTime start,
            @RequestParam(name = "start", required = false)LocalDateTime end
            ){
        return ApiResponse.success(merchantReportService.getWeeklyRevenue(authentication.getName(), storeId,start, end ));
    }
}
