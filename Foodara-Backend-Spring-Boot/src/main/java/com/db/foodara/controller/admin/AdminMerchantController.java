package com.db.foodara.controller.admin;

import com.db.foodara.dto.request.admin.UpdateMerchantApprovalRequest;
import com.db.foodara.dto.request.admin.UpdateStoreStatusRequest;
import com.db.foodara.dto.request.admin.VerifyDocumentRequest;
import com.db.foodara.dto.response.ApiResponse;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.*;
import com.db.foodara.service.admin.AdminMerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    // --- Merchants ---

    @GetMapping("/merchants")
    public ApiResponse<PageResponse<AdminMerchantResponse>> getMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminMerchantService.getMerchants(page, size, search, status));
    }

    @GetMapping("/merchants/{id}")
    public ApiResponse<AdminMerchantResponse> getMerchantDetail(@PathVariable String id) {
        return ApiResponse.success(adminMerchantService.getMerchantDetail(id));
    }

    @PutMapping("/merchants/{id}/approval")
    public ApiResponse<Void> updateMerchantApproval(@PathVariable String id,
                                                    @RequestBody @Valid UpdateMerchantApprovalRequest request) {
        adminMerchantService.updateMerchantApproval(id, request);
        return ApiResponse.success("Merchant approval status updated");
    }

    @GetMapping("/merchants/{id}/documents")
    public ApiResponse<List<AdminStoreDocumentResponse>> getMerchantDocuments(@PathVariable String id) {
        return ApiResponse.success(adminMerchantService.getMerchantDocuments(id));
    }

    @GetMapping("/merchants/{id}/bank-accounts")
    public ApiResponse<List<AdminStoreBankAccountResponse>> getMerchantBankAccounts(@PathVariable String id) {
        return ApiResponse.success(adminMerchantService.getMerchantBankAccounts(id));
    }

    // --- Stores ---

    @GetMapping("/stores")
    public ApiResponse<PageResponse<AdminStoreResponse>> getStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {
        return ApiResponse.success(adminMerchantService.getStores(page, size, search, isActive));
    }

    @GetMapping("/stores/{id}")
    public ApiResponse<AdminStoreResponse> getStoreDetail(@PathVariable String id) {
        return ApiResponse.success(adminMerchantService.getStoreDetail(id));
    }

    @PutMapping("/stores/{id}/status")
    public ApiResponse<Void> updateStoreStatus(@PathVariable String id,
                                               @RequestBody @Valid UpdateStoreStatusRequest request) {
        adminMerchantService.updateStoreStatus(id, request);
        return ApiResponse.success("Store status updated");
    }

    @GetMapping("/stores/{id}/operating-hours")
    public ApiResponse<List<AdminStoreOperatingHoursResponse>> getStoreOperatingHours(@PathVariable String id) {
        return ApiResponse.success(adminMerchantService.getStoreOperatingHours(id));
    }

    @GetMapping("/stores/{id}/documents")
    public ApiResponse<List<AdminStoreDocumentResponse>> getStoreDocuments(@PathVariable String id) {
        return ApiResponse.success(adminMerchantService.getStoreDocuments(id));
    }

    // --- Documents ---

    @PutMapping("/documents/{id}/verify")
    public ApiResponse<Void> verifyDocument(@PathVariable String id,
                                            @RequestBody @Valid VerifyDocumentRequest request) {
        adminMerchantService.verifyDocument(id, request);
        return ApiResponse.success("Document verified");
    }
}
