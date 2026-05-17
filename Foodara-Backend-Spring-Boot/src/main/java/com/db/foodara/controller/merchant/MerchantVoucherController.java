package com.db.foodara.controller.merchant;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.foodara.dto.request.merchant.MerchantCampaignJoinRequest;
import com.db.foodara.dto.request.merchant.MerchantVoucherRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.merchant.MerchantCampaignJoinResponse;
import com.db.foodara.dto.response.promotion.VoucherResponse;
import com.db.foodara.service.merchant.MerchantVoucherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * M08 — Merchant Promotions endpoints.
 * - Store vouchers CRUD: /v1/merchant/vouchers
 * - Campaign participation: /v1/merchant/campaigns/join
 */
@RestController
@RequestMapping("/v1/merchant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantVoucherController {

    private final MerchantVoucherService merchantVoucherService;

    @GetMapping("/vouchers")
    public ApiResponse<List<VoucherResponse>> getVouchers(Authentication authentication) {
        return ApiResponse.success(merchantVoucherService.getVouchers(authentication.getName()));
    }

    @PostMapping("/vouchers/{storeId}")
    public ApiResponse<VoucherResponse> createVoucher(
            Authentication authentication,
            @PathVariable String storeId,
            @RequestBody @Valid MerchantVoucherRequest request) {
        return ApiResponse.success(merchantVoucherService.createVoucher(authentication.getName(), storeId, request));
    }

    @PutMapping("/vouchers/{id}")
    public ApiResponse<VoucherResponse> updateVoucher(
            Authentication authentication,
            @PathVariable String id,
            @RequestBody @Valid MerchantVoucherRequest request) {
        return ApiResponse.success(merchantVoucherService.updateVoucher(authentication.getName(), id, request));
    }

    @DeleteMapping("/vouchers/{id}")
    public ApiResponse<Void> deleteVoucher(Authentication authentication, @PathVariable String id) {
        merchantVoucherService.deleteVoucher(authentication.getName(), id);
        return ApiResponse.success("Voucher deleted");
    }

    @GetMapping("/campaigns/join")
    public ApiResponse<List<MerchantCampaignJoinResponse>> getJoinedCampaigns(Authentication authentication) {
        return ApiResponse.success(merchantVoucherService.getJoinedCampaigns(authentication.getName()));
    }

    @PostMapping("/campaigns/join")
    public ApiResponse<MerchantCampaignJoinResponse> joinCampaign(
            Authentication authentication,
            @RequestBody @Valid MerchantCampaignJoinRequest request) {
        return ApiResponse.success(merchantVoucherService.joinCampaign(authentication.getName(), request));
    }
}
