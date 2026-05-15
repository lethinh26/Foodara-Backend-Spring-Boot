package com.db.foodara.controller.promotion;

import com.db.foodara.dto.request.promotion.VoucherApplyRequest;
import com.db.foodara.dto.request.promotion.VoucherRemoveRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.promotion.VoucherBestChoiceResponse;
import com.db.foodara.dto.response.promotion.VoucherCartPricingResponse;
import com.db.foodara.dto.response.promotion.VoucherResponse;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.service.promotion.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/stores/{id}/vouchers")
    public ApiResponse<List<VoucherResponse>> getStoreVouchers(
            @PathVariable("id") String storeId,
            @RequestParam(required = false) BigDecimal subtotal,
            Authentication authentication
    ) {
        String userId = authentication != null ? authentication.getName() : null;
        return ApiResponse.success(voucherService.getStoreVouchers(storeId, userId, subtotal));
    }


    @GetMapping("/vouchers/available")
    public ApiResponse<VoucherCartPricingResponse> getAvailableForCart(
            Authentication authentication,
            @RequestParam String storeId
    ) {
        return ApiResponse.success(voucherService.getAvailableForCart(requireUserId(authentication), storeId));
    }

    @PostMapping("/vouchers/apply")
    public ApiResponse<VoucherCartPricingResponse> applyVouchers(
            Authentication authentication,
            @Valid @RequestBody VoucherApplyRequest request
    ) {
        return ApiResponse.success(voucherService.applyVouchersForCart(requireUserId(authentication), request));
    }

    @PostMapping("/vouchers/remove")
    public ApiResponse<VoucherCartPricingResponse> removeVouchers(
            Authentication authentication,
            @Valid @RequestBody VoucherRemoveRequest request
    ) {
        return ApiResponse.success(voucherService.removeVouchersForCart(requireUserId(authentication), request));
    }

    @GetMapping("/vouchers/my-vouchers")
    public ApiResponse<List<VoucherResponse>> getMyVouchers(
            Authentication authentication,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) BigDecimal subtotal
    ) {
        return ApiResponse.success(voucherService.getMyVouchers(requireUserId(authentication), storeId, subtotal));
    }

    @PostMapping("/vouchers/{id}/collect")
    public ApiResponse<VoucherResponse> collectVoucher(
            Authentication authentication,
            @PathVariable("id") String voucherId
    ) {
        return ApiResponse.success(voucherService.collectVoucher(requireUserId(authentication), voucherId));
    }

    @GetMapping("/vouchers/best")
    public ApiResponse<VoucherBestChoiceResponse> getBestVoucher(
            Authentication authentication,
            @RequestParam String storeId,
            @RequestParam BigDecimal subtotal
    ) {
        return ApiResponse.success(voucherService.getBestVoucherForStore(requireUserId(authentication), storeId, subtotal));
    }

    private String requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
