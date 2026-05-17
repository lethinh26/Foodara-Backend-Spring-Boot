package com.db.foodara.controller.store;

import com.db.foodara.dto.request.store.CreateReviewRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.store.CustomerReviewResponse;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.service.store.CustomerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerReviewController {

    private final CustomerReviewService customerReviewService;

    /** POST /v1/reviews — create a new review for an order. */
    @PostMapping("/v1/reviews")
    public ApiResponse<CustomerReviewResponse> createReview(
            Authentication authentication,
            @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success(customerReviewService.createReview(requireUserId(authentication), request));
    }

    /** GET /v1/reviews/{id} — get review detail (owner only). */
    @GetMapping("/v1/reviews/{id}")
    public ApiResponse<CustomerReviewResponse> getReview(
            Authentication authentication,
            @PathVariable String id) {
        return ApiResponse.success(customerReviewService.getReview(requireUserId(authentication), id));
    }

    /** GET /v1/reviews/order/{orderId} — check if user already reviewed this order. */
    @GetMapping("/v1/reviews/order/{orderId}")
    public ApiResponse<CustomerReviewResponse> getReviewByOrder(
            Authentication authentication,
            @PathVariable String orderId) {
        CustomerReviewResponse review = customerReviewService.getReviewByOrder(requireUserId(authentication), orderId);
        return ApiResponse.success(review); // null result means not reviewed yet
    }

    /** GET /v1/stores/{storeId}/reviews — public list of active reviews for a store. */
    @GetMapping("/v1/stores/{storeId}/reviews")
    public ApiResponse<List<CustomerReviewResponse>> getStoreReviews(@PathVariable String storeId) {
        return ApiResponse.success(customerReviewService.getStoreReviews(storeId));
    }

    private String requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
