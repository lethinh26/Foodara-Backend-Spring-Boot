package com.db.foodara.driver.controller;

import com.db.foodara.driver.dto.request.IncentiveProgramRequest;
import com.db.foodara.driver.dto.response.*;
import com.db.foodara.driver.service.AdminDriverIncentiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/incentive-programs")
@RequiredArgsConstructor
public class AdminDriverIncentiveController {

    private final AdminDriverIncentiveService incentiveService;

    @GetMapping
    public ApiResponse<PageResponse<DriverIncentiveProgramResponse>> getPrograms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        return ApiResponse.success(incentiveService.getPrograms(page, size, search, isActive));
    }

    @PostMapping
    public ApiResponse<DriverIncentiveProgramResponse> createProgram(
            @RequestBody @Valid IncentiveProgramRequest request) {
        return ApiResponse.success(incentiveService.createProgram(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DriverIncentiveProgramResponse> updateProgram(
            @PathVariable String id,
            @RequestBody @Valid IncentiveProgramRequest request) {
        return ApiResponse.success(incentiveService.updateProgram(id, request));
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<PageResponse<DriverIncentiveProgressResponse>> getProgramProgress(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ApiResponse.success(incentiveService.getProgramProgress(id, page, size));
    }
}
