package com.db.foodara.controller.merchant;

import com.db.foodara.dto.reponse.ApiResponse;
import com.db.foodara.dto.reponse.auth.TokenResponse;
import com.db.foodara.dto.reponse.merchant.MerchantProfileResponse;
import com.db.foodara.dto.request.auth.RegisterRequest;
import com.db.foodara.dto.request.merchant.MerchantRegisterRequest;
import com.db.foodara.dto.request.merchant.MerchantUpdateProfileRequest;
import com.db.foodara.dto.request.merchant.bankaccount.BankAccountCreateRequest;
import com.db.foodara.dto.request.merchant.bankaccount.BankAccountUpdateRequest;
import com.db.foodara.dto.request.merchant.document.DocumentRequest;
import com.db.foodara.dto.request.store.StoreAddressRequest;
import com.db.foodara.dto.request.store.StoreCreateRequest;
import com.db.foodara.dto.request.store.StoreOperationRequest;
import com.db.foodara.dto.request.store.StoreStatusRequest;
import com.db.foodara.entity.merchant.Merchant;
import com.db.foodara.entity.merchant.bankaccount.BankAccount;
import com.db.foodara.entity.merchant.document.Document;
import com.db.foodara.entity.merchant.store.Store;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.service.merchant.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/merchant")
@RequiredArgsConstructor
public class MerchantController {
    private final MerchantService merchantService;

    // 83 post	/api/merchant/register
    @PostMapping("/register")
    public ApiResponse<Merchant> registerMerchant(@RequestBody @Valid MerchantRegisterRequest request) {
        return ApiResponse.success("Register merchant successful", merchantService.registerMerchant(request));
    }

    // 84	GET 	/api/merchant/profile	/api/merchant/profile
    @GetMapping("/profile/{merchantId}")
    public ApiResponse<MerchantProfileResponse> merchantProfile(@PathVariable String merchantId) {
        return ApiResponse.success("Get merchant profile successful", merchantService.getMerchantProfile(merchantId));
    }

    // 85	PUT	/api/merchant/profile
    @PutMapping("/profile/{merchantId}")
    public ApiResponse<MerchantProfileResponse> postMerchantProfile(@PathVariable String merchantId,@RequestBody @Valid MerchantUpdateProfileRequest request){
        return ApiResponse.success("Update merchant profile successful", merchantService.updateProfile(merchantId, request));
    }

    // 86	POST	/api/merchant/documents
    @PostMapping("/documents/{merchantId}")
    public ApiResponse<Document> uploadDocument(@PathVariable String merchantId, @RequestBody DocumentRequest request){
        return ApiResponse.success("Upload document successful", merchantService.uploadDocument(merchantId, request));
    }

    // 87	GET	/api/merchant/documents
    @GetMapping("/documents/{merchantId}")
    public ApiResponse<List<Document>> getAllDocumentOfMerchant(@PathVariable String merchantId){
        return ApiResponse.success("Get all document of merchant successful", merchantService.getAllDocumentOfMerchant(merchantId));
    }

    // 88	GET	/api/merchant/stores
    @GetMapping("/stores/{merchantId}")
    public ApiResponse<List<Store>> getStoresOfMerchant(@PathVariable String merchantId){
        return ApiResponse.success("Get stores of merchant successful", merchantService.getStoresOfMerchant(merchantId));
    }

    // 89 POST /api/merchant/stores
    @PostMapping("/stores/{merchantId}")
    public ApiResponse<Store> postStore(@PathVariable String merchantId,@RequestBody StoreCreateRequest request){
        return ApiResponse.success("Register a store successful", merchantService.createStore(merchantId, request));
    }

    // 90 GET	/api/merchant/store/:id
    @GetMapping("/store/{storeId}")
    public ApiResponse<Store> getStoreDetail(@PathVariable String storeId){
        return ApiResponse.success("Get store detail successful", merchantService.getStoreDetail(storeId));
    }

    // 91 PUT	/api/merchant/stores/:id
    @PutMapping("/store-address/{storeId}")
    public ApiResponse<Store> updateStore(@PathVariable String storeId, @RequestBody @Valid StoreAddressRequest request){
        return ApiResponse.success("Update store address successful", merchantService.updateStoreAddress(storeId, request));
    }

    // 92 put /api/merchant/stores/:id/toggle
    @PutMapping("/store-toggle/{storeId}")
    public ApiResponse<Store> updateStoreToggle(@PathVariable String storeId, @RequestBody @Valid StoreStatusRequest request){
        return ApiResponse.success("Update store toggle successful", merchantService.updateStatusStore(storeId, request));
    }

    // 93 	PUT	/api/merchant/stores/:id/operating-hours
    @PutMapping("/store-operating/{storeId}")
    public ApiResponse<Store> updateStoreOperating(@PathVariable String storeId, @RequestBody @Valid StoreOperationRequest request){
        return ApiResponse.success("Update store operation successful", merchantService.updateOperationStore(storeId, request));
    }

    // 94	GET	/api/merchant/bank-accounts
    @GetMapping("/bank-accounts/{merchantId}")
    public ApiResponse<List<BankAccount>> getBankAccounts(@PathVariable String merchantId){
        return ApiResponse.success("Get bank accounts successful", merchantService.getBankAccountOfMerchant(merchantId));
    }

    // 95	POST	/api/merchant/bank-accounts
    @PostMapping("/bank-account/{merchantId}/{storeId}")
    public ApiResponse<BankAccount> postBankAccount(@PathVariable String merchantId, @PathVariable String storeId, @RequestBody @Valid BankAccountCreateRequest request){
        return ApiResponse.success("Post a bank account successful", merchantService.createBankAccount(merchantId, storeId ,request));
    }

    // 96	PUT	/api/merchant/bank-accounts/:id
    @PutMapping("/bank-account/{bankAccountId}")
    public ApiResponse<BankAccount> updateBankAccount(@PathVariable String bankAccountId, BankAccountUpdateRequest request){
        return ApiResponse.success("Update bank account successful", merchantService.updateBankAccount(bankAccountId, request));
    }

}
