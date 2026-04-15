package com.db.foodara.controller.merchant;

import com.db.foodara.dto.request.merchant.*;
import com.db.foodara.dto.response.merchant.BankAccountResponse;
import com.db.foodara.dto.response.merchant.MerchantDocumentResponse;
import com.db.foodara.dto.response.merchant.MerchantProfileResponse;
import com.db.foodara.dto.response.store.StoreResponse;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.service.merchant.MerchantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @PostMapping("/register")
    public ApiResponse<MerchantProfileResponse> registerMerchant(Authentication authentication,
                                                                   @RequestBody @Valid MerchantRegisterRequest request) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.registerMerchant(userId, request));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<MerchantProfileResponse> getProfile(Authentication authentication) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.getProfile(userId));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<MerchantProfileResponse> updateProfile(Authentication authentication,
                                                               @RequestBody MerchantProfileRequest request) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.updateProfile(userId, request));
    }

    @PostMapping("/documents")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<MerchantDocumentResponse> uploadDocument(Authentication authentication,
                                                                 @RequestBody @Valid MerchantDocumentRequest request) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.uploadDocument(userId, request));
    }

    @GetMapping("/documents")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<List<MerchantDocumentResponse>> getDocuments(Authentication authentication) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.getDocuments(userId));
    }

    @GetMapping("/stores")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<List<StoreResponse>> getStores(Authentication authentication) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.getStores(userId));
    }

    @PostMapping("/stores")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<StoreResponse> createStore(Authentication authentication,
                                                   @RequestBody @Valid StoreCreateRequest request) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.createStore(userId, request));
    }

    @GetMapping("/stores/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<StoreResponse> getStore(Authentication authentication, @PathVariable String id) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.getStore(userId, id));
    }

    @PutMapping("/stores/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<StoreResponse> updateStore(Authentication authentication,
                                                   @PathVariable String id,
                                                   @RequestBody StoreUpdateRequest request) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.updateStore(userId, id, request));
    }

    @PutMapping("/stores/{id}/toggle")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<StoreResponse> toggleStore(Authentication authentication, @PathVariable String id) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.toggleStore(userId, id));
    }

    @PutMapping("/stores/{id}/operating-hours")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<Void> updateOperatingHours(Authentication authentication,
                                                     @PathVariable String id,
                                                     @RequestBody List<StoreOperatingHoursRequest> requests) {
        String userId = authentication.getName();
        merchantService.updateOperatingHours(userId, id, requests);
        return ApiResponse.success("Operating hours updated");
    }

    @GetMapping("/bank-accounts")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<List<BankAccountResponse>> getBankAccounts(Authentication authentication) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.getBankAccounts(userId));
    }

    @PostMapping("/bank-accounts")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<BankAccountResponse> addBankAccount(Authentication authentication,
                                                             @RequestBody @Valid BankAccountRequest request) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.addBankAccount(userId, request));
    }

    @PutMapping("/bank-accounts/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ApiResponse<BankAccountResponse> updateBankAccount(Authentication authentication,
                                                                @PathVariable String id,
                                                                @RequestBody BankAccountRequest request) {
        String userId = authentication.getName();
        return ApiResponse.success(merchantService.updateBankAccount(userId, id, request));
    }
}
