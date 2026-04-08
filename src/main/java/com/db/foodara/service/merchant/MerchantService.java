package com.db.foodara.service.merchant;

import com.db.foodara.dto.reponse.merchant.MerchantProfileResponse;
import com.db.foodara.dto.request.merchant.MerchantRegisterRequest;
import com.db.foodara.dto.request.merchant.MerchantUpdateProfileRequest;
import com.db.foodara.dto.request.merchant.bankaccount.BankAccountCreateRequest;
import com.db.foodara.dto.request.merchant.bankaccount.BankAccountUpdateRequest;
import com.db.foodara.dto.request.merchant.document.DocumentRequest;
import com.db.foodara.dto.request.store.StoreAddressRequest;
import com.db.foodara.dto.request.store.StoreCreateRequest;
import com.db.foodara.dto.request.store.StoreOperationRequest;
import com.db.foodara.dto.request.store.StoreStatusRequest;
import com.db.foodara.entity.merchant.bankaccount.BankAccount;
import com.db.foodara.entity.merchant.ApprovalMerchantStatus;
import com.db.foodara.entity.merchant.Merchant;
import com.db.foodara.entity.merchant.document.Document;
import com.db.foodara.entity.merchant.store.Store;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.repository.merchant.bankaccount.BankAccountRepository;
import com.db.foodara.repository.merchant.document.DocumentRepository;
import com.db.foodara.repository.merchant.store.StoreRepository;
import com.db.foodara.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantService {
    @Autowired
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final DocumentRepository documentRepository;
    private final BankAccountRepository bankAccountRepository;

    // 83 post -> /api/merchant/register datest
    @Transactional
    public Merchant registerMerchant(MerchantRegisterRequest request) {
        System.out.println(request);
        // merchant response = merchant
        if (request.getBusinessPhone() == null || merchantRepository.existsMerchantByBusinessPhone(request.getBusinessPhone())) {
            throw new AppException(ErrorCode.MERCHANT_PHONE_INVALID);
        }
        if (request.getBusinessEmail() == null || merchantRepository.existsMerchantByBusinessEmail(request.getBusinessEmail())) {
            throw new AppException(ErrorCode.MERCHANT_EMAIL_INVALID);
        }
        if (request.getOwnerId() == null || !userRepository.existsById(request.getOwnerId())) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Merchant merchant = new Merchant();
        if (request.getName() != null) merchant.setName(request.getName());
        if (request.getOwnerId() != null) merchant.setOwnerId(request.getOwnerId());
        if (request.getBusinessEmail() != null) merchant.setBusinessEmail(request.getBusinessEmail());
        if (request.getBusinessPhone() != null) merchant.setBusinessPhone(request.getBusinessPhone());
        if (request.getTaxCode() != null) merchant.setTaxCode(request.getTaxCode());

        return merchantRepository.save(merchant);
    }

    // 84 get merchant profile - xem thông tin merchant -> /api/merchant/profile
    public MerchantProfileResponse getMerchantProfile(String merchantId) {
        System.out.println(merchantId);
        if (merchantId == null) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Merchant merchant = merchantRepository.findMerchantById(merchantId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        return mapToMerchantProfileResponse(merchant);
    }

    // 85 UPDATE /api/merchant/profile
    @Transactional
    public MerchantProfileResponse updateProfile(String merchantRequestId, MerchantUpdateProfileRequest request) {
        Merchant merchant = merchantRepository.findMerchantById(merchantRequestId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        if (request.getName() != null) merchant.setName(request.getName());
        if (request.getBusinessEmail() != null && merchantRepository.existsMerchantByBusinessEmail(request.getBusinessEmail()))
            merchant.setBusinessEmail(request.getBusinessEmail());
        if (request.getBusinessPhone() != null && merchantRepository.existsMerchantByBusinessPhone(request.getBusinessPhone()))
            merchant.setBusinessPhone(request.getBusinessPhone());
        if (request.getTaxCode() != null) merchant.setTaxCode(request.getTaxCode());
        if (request.getLogoUrl() != null) merchant.setLogoUrl(request.getLogoUrl());
        if (request.getCoverImageUrl() != null) merchant.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getLogoUrl() != null) merchant.setLogoUrl(request.getLogoUrl());

        merchantRepository.save(merchant);

        return mapToMerchantProfileResponse(merchant);
    }

    // 86	POST	/api/merchant/documents
    @Transactional
    public Document uploadDocument(String merchantId, DocumentRequest request) {
        Document document = new Document();
        if(merchantId != null && merchantRepository.existsMerchantById(merchantId)) document.setMerchantId(merchantId);
        System.out.println(request.getDocumentType() + " " + request.getVerificationStatus());
        if (request.getDocumentType() != null && request.getVerificationStatus() != null) {
            document.setDocumentType(request.getDocumentType());
            document.setVerificationStatus(request.getVerificationStatus());
        } else {
            throw new AppException(ErrorCode.DOCUMENT_INVALID);
        }
        return documentRepository.save(document);
    }

    // 87 GET /api/merchant/documents
    public List<Document> getAllDocumentOfMerchant(String merchantId) {
        return documentRepository.getDocumentsByMerchantId(merchantId).orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    // 88 GET /api/merchant/stores
    public List<Store> getStoresOfMerchant(String merchantId) {
        return storeRepository.getStoresByMerchantId(merchantId);
    }

    // 89 POST /api/merchant/stores
    @Transactional
    public Store createStore(String merchantId, StoreCreateRequest request) {
        Store store = new Store();

        if (merchantId != null && merchantRepository.existsMerchantById(merchantId))
            store.setMerchantId(merchantId);
        if (request.getName() != null && !storeRepository.existsByName(request.getName()))
            store.setName(request.getName());
        if (request.getSlug() != null && !storeRepository.existsBySlug(request.getSlug()))
            store.setSlug(request.getSlug());
        if (request.getAddressLine() != null) store.setAddressLine(request.getAddressLine());
        if (request.getWard() != null) store.setWard(request.getWard());
        if (request.getDistrictId() != null) store.setDistrictId(request.getDistrictId());
        if (request.getCity_id() != null) store.setCity_id(request.getCity_id());
        if (request.getLatitude() != null) store.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) store.setLongitude(request.getLongitude());
        if (request.getServiceZone() != null) store.setServiceZone(request.getServiceZone());
        // chua  check  E>  city district
        return storeRepository.save(store);
    }

    // 90 GET	/api/merchant/stores/:id
    public Store getStoreDetail(String storeId) {
        return storeRepository.getStoreById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
    }

    // 91 PUT	/api/merchant/stores/:id
    @Transactional
    public Store updateStoreAddress(String id, StoreAddressRequest request) {
        Store store = storeRepository.getStoreById(id).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        if (request.getAddressLine() != null) store.setAddressLine(request.getAddressLine());
        if (request.getWard() != null) store.setWard(request.getWard());
        if (request.getDistrictId() != null) store.setDistrictId(request.getDistrictId());
        if (request.getCity_id() != null) store.setCity_id(request.getCity_id());
        if (request.getLatitude() != null) store.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) store.setLongitude(request.getLongitude());
        if (request.getServiceZone() != null) store.setServiceZone(request.getServiceZone());

        return storeRepository.save(store);
    }

    // 92 put /api/merchant/stores/:id/toggle
    @Transactional
    public Store updateStatusStore(String id, StoreStatusRequest request) {
        Store store = storeRepository.getStoreById(id).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        store.setActive(request.isActive());
        store.setOpen(request.isOpen());
        store.setAutoAcceptOrders(request.isAutoAcceptOrders());
        return storeRepository.save(store);
    }

    // 93 	PUT	/api/merchant/stores/:id/operating-hours
    @Transactional
    public Store updateOperationStore(String id, StoreOperationRequest request) {
        Store store = storeRepository.getStoreById(id).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        store.setAvgPreparationTime(request.getAvgPreparationTime());
        store.setMinOrderAmount(request.getMinOrderAmount());
        store.setMaxDeliveryRadiusKm(request.getMaxDeliveryRadiusKm());
        return storeRepository.save(store);
    }

    // 94	GET	/api/merchant/bank-accounts
    public List<BankAccount> getBankAccountOfMerchant(String merchantId) {
        return bankAccountRepository.getBankAccountsByAccountHolder(merchantId).orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
    }

    // 95	POST	/api/merchant/bank-accounts
    @Transactional
    public BankAccount createBankAccount(String merchantId, String storeId, BankAccountCreateRequest request) {
        if (!merchantRepository.existsMerchantById(merchantId)) {
            throw new AppException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        if (!storeRepository.existsByMerchantIdAndId(merchantId, storeId)) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }
        if (request.getAccountNumber() != null && bankAccountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new AppException(ErrorCode.BANK_ACCOUNT_ALREADY_EXISTS);
        }

        BankAccount bankAccount = new BankAccount();

        if (request.getBankName() != null) {
            bankAccount.setBankName(request.getBankName());
        }

        bankAccount.setAccountNumber(request.getAccountNumber());
        bankAccount.setAccountHolder(merchantId);
        bankAccount.setBranch(storeId);
        return bankAccountRepository.save(bankAccount);
    }

    // 96	PUT	/api/merchant/bank-accounts/:id
    @Transactional
    public BankAccount updateBankAccount(String id, BankAccountUpdateRequest request) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
        if (request.getBankName() != null) {
            bankAccount.setBankName(request.getBankName());
        }
        if (request.getBranch() != null) {
            if (!storeRepository.existsById(request.getBranch())) {
                throw new AppException(ErrorCode.STORE_NOT_FOUND);
            }
            bankAccount.setBranch(request.getBranch());
        }
        return bankAccountRepository.save(bankAccount);
    }

    // merchant -> merchantProfile
    private MerchantProfileResponse mapToMerchantProfileResponse(Merchant merchant) {
        return MerchantProfileResponse.builder()
                .name(merchant.getName())
                .taxCode(merchant.getTaxCode())
                .businessEmail(merchant.getBusinessEmail())
                .businessPhone(merchant.getBusinessPhone())
                .logoUrl(merchant.getLogoUrl())
                .coverImageUrl(merchant.getCoverImageUrl())
                .approvalStatus(merchant.getApprovalStatus())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    // confirm merchant // update ApprovalMerchantStatus
    public String responseRequestMerchant(String id, ApprovalMerchantStatus reques) {
        Merchant merchant = merchantRepository.findMerchantById(id).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        merchant.setApprovalStatus(reques);
        merchantRepository.save(merchant);
        return reques.toString();
    }

    // update status (approval) -> cho admin
    @Transactional
    public MerchantProfileResponse updateStatus(String merchantRequestId, ApprovalMerchantStatus request) {
        Merchant merchant = merchantRepository.findMerchantById(merchantRequestId).orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        if (request != null) merchant.setApprovalStatus(request);
        merchantRepository.save(merchant);

        return mapToMerchantProfileResponse(merchant);
    }

}
