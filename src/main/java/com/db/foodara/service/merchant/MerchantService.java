package com.db.foodara.service.merchant;

import com.db.foodara.dto.request.merchant.*;
import com.db.foodara.dto.response.merchant.BankAccountResponse;
import com.db.foodara.dto.response.merchant.MerchantDocumentResponse;
import com.db.foodara.dto.response.merchant.MerchantProfileResponse;
import com.db.foodara.dto.response.store.StoreResponse;
import com.db.foodara.entity.merchant.*;
import com.db.foodara.entity.store.Store;
import com.db.foodara.entity.role.Role;
import com.db.foodara.entity.user.UserRole;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.*;
import com.db.foodara.repository.store.StoreRepository;
import com.db.foodara.repository.role.RoleRepository;
import com.db.foodara.repository.user.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreDocumentRepository storeDocumentRepository;

    @Autowired
    private StoreBankAccountRepository storeBankAccountRepository;

    @Autowired
    private StoreOperatingHoursRepository storeOperatingHoursRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Transactional
    public MerchantProfileResponse registerMerchant(String userId, MerchantRegisterRequest request) {
        if (merchantRepository.existsByOwnerId(userId)) {
            throw new AppException(ErrorCode.MERCHANT_ALREADY_EXISTS);
        }

        // nếu user tồn tại

        Role merchantRole = roleRepository.findByNameIgnoreCase("MERCHANT")
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        boolean hasMerchantRole = userRoleRepository.findByUserId(userId).stream()
                .anyMatch(ur -> ur.getRoleId().equals(merchantRole.getId()));

        if (!hasMerchantRole) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(merchantRole.getId());
            userRoleRepository.save(userRole);
        }

        Merchant merchant = new Merchant();
        merchant.setOwnerId(userId);
        merchant.setName(request.getName());
        merchant.setTaxCode(request.getTaxCode());
        merchant.setBusinessEmail(request.getBusinessEmail());
        merchant.setBusinessPhone(request.getBusinessPhone());
        merchant.setLogoUrl(request.getLogoUrl());
        merchant.setCoverImageUrl(request.getCoverImageUrl());
        merchant.setApprovalStatus("pending");

        UserRole userRole = new UserRole();
        userRole.setUserId(merchant.getId());

        Merchant saved = merchantRepository.save(merchant);
        return mapToMerchantProfileResponse(saved);
    }

    public MerchantProfileResponse getProfile(String userId) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        return mapToMerchantProfileResponse(merchant);
    }

    public boolean isMerchant(String userId) {
        return merchantRepository.existsByOwnerId(userId);
    }

    @Transactional
    public MerchantProfileResponse updateProfile(String userId, MerchantProfileRequest request) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        if (request.getName() != null) {
            merchant.setName(request.getName());
        }
        if (request.getTaxCode() != null) {
            merchant.setTaxCode(request.getTaxCode());
        }
        if (request.getBusinessEmail() != null) {
            merchant.setBusinessEmail(request.getBusinessEmail());
        }
        if (request.getBusinessPhone() != null) {
            merchant.setBusinessPhone(request.getBusinessPhone());
        }
        if (request.getLogoUrl() != null) {
            merchant.setLogoUrl(request.getLogoUrl());
        }
        if (request.getCoverImageUrl() != null) {
            merchant.setCoverImageUrl(request.getCoverImageUrl());
        }

        Merchant updated = merchantRepository.save(merchant);
        return mapToMerchantProfileResponse(updated);
    }

    @Transactional
    public MerchantDocumentResponse uploadDocument(String userId, MerchantDocumentRequest request) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        StoreDocument document = new StoreDocument();
        document.setMerchantId(merchant.getId());
        document.setStoreId(request.getStoreId());
        document.setDocumentType(request.getDocumentType());
        document.setDocumentUrl(request.getDocumentUrl());
        document.setDocumentNumber(request.getDocumentNumber());
        document.setExpiryDate(request.getExpiryDate());
        document.setVerificationStatus("pending");

        StoreDocument saved = storeDocumentRepository.save(document);
        return mapToDocumentResponse(saved);
    }

    public List<MerchantDocumentResponse> getDocuments(String userId) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        return storeDocumentRepository.findByMerchantId(merchant.getId()).stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StoreResponse createStore(String userId, StoreCreateRequest request) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        Store store = new Store();
        store.setMerchantId(merchant.getId());
        store.setName(request.getName());
        store.setSlug(request.getSlug());
        store.setDescription(request.getDescription());
        store.setPhone(request.getPhone());
        store.setAddressLine(request.getAddressLine());
        store.setWard(request.getWard());
        store.setDistrictName(request.getDistrictName());
        store.setCityName(request.getCityName());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        store.setAutoAcceptOrders(request.getAutoAcceptOrders() != null ? request.getAutoAcceptOrders() : false);
        store.setAvgPreparationTime(request.getAvgPreparationTime() != null ? request.getAvgPreparationTime() : 15);
        store.setMinOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : java.math.BigDecimal.ZERO);
        store.setMaxDeliveryRadiusKm(request.getMaxDeliveryRadiusKm());
        store.setCoverImageUrl(request.getCoverImageUrl());
        store.setLogoUrl(request.getLogoUrl());
        store.setIsOpen(false);
        store.setIsActive(true);

        Store saved = storeRepository.save(store);
        return mapToStoreResponse(saved);
    }

    public List<StoreResponse> getStores(String userId) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        return storeRepository.findByMerchantId(merchant.getId()).stream()
                .map(this::mapToStoreResponse)
                .collect(Collectors.toList());
    }

    public StoreResponse getStore(String userId, String storeId) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        Store store = storeRepository.findByIdAndMerchantId(storeId, merchant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        return mapToStoreResponse(store);
    }

    @Transactional
    public StoreResponse updateStore(String userId, String storeId, StoreUpdateRequest request) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        Store store = storeRepository.findByIdAndMerchantId(storeId, merchant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        if (request.getName() != null) store.setName(request.getName());
        if (request.getSlug() != null) store.setSlug(request.getSlug());
        if (request.getDescription() != null) store.setDescription(request.getDescription());
        if (request.getPhone() != null) store.setPhone(request.getPhone());
        if (request.getAddressLine() != null) store.setAddressLine(request.getAddressLine());
        if (request.getWard() != null) store.setWard(request.getWard());
        if (request.getDistrictName() != null) store.setDistrictName(request.getDistrictName());
        if (request.getCityName() != null) store.setCityName(request.getCityName());
        if (request.getLatitude() != null) store.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) store.setLongitude(request.getLongitude());
        if (request.getAutoAcceptOrders() != null) store.setAutoAcceptOrders(request.getAutoAcceptOrders());
        if (request.getAvgPreparationTime() != null) store.setAvgPreparationTime(request.getAvgPreparationTime());
        if (request.getMinOrderAmount() != null) store.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getMaxDeliveryRadiusKm() != null) store.setMaxDeliveryRadiusKm(request.getMaxDeliveryRadiusKm());
        if (request.getCoverImageUrl() != null) store.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getLogoUrl() != null) store.setLogoUrl(request.getLogoUrl());

        Store updated = storeRepository.save(store);
        return mapToStoreResponse(updated);
    }

    @Transactional
    public StoreResponse toggleStore(String userId, String storeId) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        Store store = storeRepository.findByIdAndMerchantId(storeId, merchant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        store.setIsOpen(!store.getIsOpen());
        Store updated = storeRepository.save(store);
        return mapToStoreResponse(updated);
    }

    @Transactional
    public void updateOperatingHours(String userId, String storeId, List<StoreOperatingHoursRequest> requests) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        Store store = storeRepository.findByIdAndMerchantId(storeId, merchant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        storeOperatingHoursRepository.deleteByStoreId(storeId);

        for (StoreOperatingHoursRequest request : requests) {
            StoreOperatingHours hours = new StoreOperatingHours();
            hours.setStoreId(store.getId());
            hours.setDayOfWeek(request.getDayOfWeek());
            hours.setOpenTime(request.getOpenTime());
            hours.setCloseTime(request.getCloseTime());
            hours.setIsClosed(request.getIsClosed() != null ? request.getIsClosed() : false);
            storeOperatingHoursRepository.save(hours);
        }
    }

    @Transactional
    public BankAccountResponse addBankAccount(String userId, BankAccountRequest request) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            storeBankAccountRepository.findByMerchantId(merchant.getId())
                    .forEach(acc -> {
                        acc.setIsDefault(false);
                        storeBankAccountRepository.save(acc);
                    });
        }

        StoreBankAccount bankAccount = new StoreBankAccount();
        bankAccount.setMerchantId(merchant.getId());
        bankAccount.setBankName(request.getBankName());
        bankAccount.setAccountNumber(request.getAccountNumber());
        bankAccount.setAccountHolder(request.getAccountHolder());
        bankAccount.setBranch(request.getBranch());
        bankAccount.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        bankAccount.setIsVerified(false);

        StoreBankAccount saved = storeBankAccountRepository.save(bankAccount);
        return mapToBankAccountResponse(saved);
    }

    public List<BankAccountResponse> getBankAccounts(String userId) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        return storeBankAccountRepository.findByMerchantId(merchant.getId()).stream()
                .map(this::mapToBankAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BankAccountResponse updateBankAccount(String userId, String accountId, BankAccountRequest request) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        StoreBankAccount bankAccount = storeBankAccountRepository.findByIdAndMerchantId(accountId, merchant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_BANK_ACCOUNT_NOT_FOUND));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            storeBankAccountRepository.findByMerchantId(merchant.getId())
                    .forEach(acc -> {
                        if (!acc.getId().equals(accountId)) {
                            acc.setIsDefault(false);
                            storeBankAccountRepository.save(acc);
                        }
                    });
        }

        if (request.getBankName() != null) bankAccount.setBankName(request.getBankName());
        if (request.getAccountNumber() != null) bankAccount.setAccountNumber(request.getAccountNumber());
        if (request.getAccountHolder() != null) bankAccount.setAccountHolder(request.getAccountHolder());
        if (request.getBranch() != null) bankAccount.setBranch(request.getBranch());
        if (request.getIsDefault() != null) bankAccount.setIsDefault(request.getIsDefault());

        StoreBankAccount updated = storeBankAccountRepository.save(bankAccount);
        return mapToBankAccountResponse(updated);
    }

    private MerchantProfileResponse mapToMerchantProfileResponse(Merchant m) {
        return MerchantProfileResponse.builder()
                .id(m.getId())
                .ownerId(m.getOwnerId())
                .name(m.getName())
                .taxCode(m.getTaxCode())
                .businessEmail(m.getBusinessEmail())
                .businessPhone(m.getBusinessPhone())
                .logoUrl(m.getLogoUrl())
                .coverImageUrl(m.getCoverImageUrl())
                .approvalStatus(m.getApprovalStatus())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private MerchantDocumentResponse mapToDocumentResponse(StoreDocument d) {
        return MerchantDocumentResponse.builder()
                .id(d.getId())
                .merchantId(d.getMerchantId())
                .storeId(d.getStoreId())
                .documentType(d.getDocumentType())
                .documentUrl(d.getDocumentUrl())
                .documentNumber(d.getDocumentNumber())
                .expiryDate(d.getExpiryDate())
                .verificationStatus(d.getVerificationStatus())
                .verifiedAt(d.getVerifiedAt())
                .verifiedBy(d.getVerifiedBy())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private StoreResponse mapToStoreResponse(Store s) {
        return StoreResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .slug(s.getSlug())
                .description(s.getDescription())
                .phone(s.getPhone())
                .addressLine(s.getAddressLine())
                .ward(s.getWard())
                .districtName(s.getDistrictName())
                .cityName(s.getCityName())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .isOpen(s.getIsOpen())
                .isActive(s.getIsActive())
                .autoAcceptOrders(s.getAutoAcceptOrders())
                .avgPreparationTime(s.getAvgPreparationTime())
                .minOrderAmount(s.getMinOrderAmount())
                .avgRating(s.getAvgRating())
                .totalRatings(s.getTotalRatings())
                .totalOrders(s.getTotalOrders())
                .coverImageUrl(s.getCoverImageUrl())
                .logoUrl(s.getLogoUrl())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private BankAccountResponse mapToBankAccountResponse(StoreBankAccount b) {
        return BankAccountResponse.builder()
                .id(b.getId())
                .merchantId(b.getMerchantId())
                .bankName(b.getBankName())
                .accountNumber(b.getAccountNumber())
                .accountHolder(b.getAccountHolder())
                .branch(b.getBranch())
                .isDefault(b.getIsDefault())
                .isVerified(b.getIsVerified())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
