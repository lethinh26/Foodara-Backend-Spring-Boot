package com.db.foodara.driver.controller;

import com.db.foodara.driver.dto.request.UpdateDriverApprovalRequest;
import com.db.foodara.driver.dto.request.VerifyDriverDocumentRequest;
import com.db.foodara.driver.dto.response.*;
import com.db.foodara.driver.service.AdminDriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminDriverController {

    private final AdminDriverService driverService;

    @GetMapping("/drivers")
    public ApiResponse<PageResponse<AdminDriverResponse>> getDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(driverService.getDrivers(page, size, search, status));
    }

    @GetMapping("/drivers/{id}")
    public ApiResponse<AdminDriverResponse> getDriverDetail(@PathVariable String id) {
        return ApiResponse.success(driverService.getDriverDetail(id));
    }

    @PutMapping("/drivers/{id}/approval")
    public ApiResponse<Void> updateDriverApproval(@PathVariable String id,
                                                  @RequestBody @Valid UpdateDriverApprovalRequest request) {
        driverService.updateDriverApproval(id, request);
        return ApiResponse.success("Driver approval status updated");
    }

    @GetMapping("/drivers/{id}/documents")
    public ApiResponse<List<DriverDocumentResponse>> getDriverDocuments(@PathVariable String id) {
        return ApiResponse.success(driverService.getDriverDocuments(id));
    }

    @PutMapping("/driver-documents/{id}/verify")
    public ApiResponse<Void> verifyDocument(@PathVariable String id,
                                            @RequestBody @Valid VerifyDriverDocumentRequest request) {
        driverService.verifyDocument(id, request);
        return ApiResponse.success("Document verified");
    }

    @GetMapping("/drivers/{id}/bank-accounts")
    public ApiResponse<List<DriverBankAccountResponse>> getDriverBankAccounts(@PathVariable String id) {
        return ApiResponse.success(driverService.getDriverBankAccounts(id));
    }

    @GetMapping("/drivers/{id}/wallet")
    public ApiResponse<PageResponse<DriverWalletTransactionResponse>> getDriverWalletTransactions(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ApiResponse.success(driverService.getDriverWalletTransactions(id, page, size));
    }

    @GetMapping("/drivers/{id}/shifts")
    public ApiResponse<PageResponse<DriverShiftResponse>> getDriverShifts(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ApiResponse.success(driverService.getDriverShifts(id, page, size));
    }
}
