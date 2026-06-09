package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.admin.DailyStatsResponse;
import com.db.foodara.dto.response.admin.DashboardSummaryResponse;
import com.db.foodara.service.admin.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardSummaryResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(adminStatsService.getDashboard(from, to));
    }

    @GetMapping("/daily")
    public ApiResponse<List<DailyStatsResponse>> getDailyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(adminStatsService.getDailyStats(from, to));
    }
}
