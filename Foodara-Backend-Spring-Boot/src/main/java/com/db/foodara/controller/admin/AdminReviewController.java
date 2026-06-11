package com.db.foodara.controller.admin;

import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminReviewResponse;
import com.db.foodara.service.admin.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ApiResponse<PageResponse<AdminReviewResponse>> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String search) {
        return ApiResponse.success(adminReviewService.getReviews(page, size, status, rating, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminReviewResponse> getReviewDetail(@PathVariable String id) {
        return ApiResponse.success(adminReviewService.getReviewDetail(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateReviewStatus(@PathVariable String id, @RequestBody Map<String, String> request) {
        String newStatus = request.get("status");
        adminReviewService.updateReviewStatus(id, newStatus);
        return ApiResponse.success("Review status updated");
    }
}
