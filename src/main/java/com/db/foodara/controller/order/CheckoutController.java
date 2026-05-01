package com.db.foodara.controller.order;

import com.db.foodara.dto.request.order.CheckoutPreviewRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.order.CheckoutDeliveryFeeResponse;
import com.db.foodara.dto.response.order.CheckoutPreviewResponse;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.service.order.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/preview")
    public ApiResponse<CheckoutPreviewResponse> preview(
            Authentication authentication,
            @Valid @RequestBody CheckoutPreviewRequest request
    ) {
        return ApiResponse.success(checkoutService.preview(requireUserId(authentication), request));
    }

    @GetMapping("/delivery-fee")
    public ApiResponse<CheckoutDeliveryFeeResponse> getDeliveryFee(
            Authentication authentication,
            @RequestParam String storeId,
            @RequestParam(required = false) String addressId
    ) {
        return ApiResponse.success(checkoutService.getDeliveryFee(requireUserId(authentication), storeId, addressId));
    }

    private String requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
