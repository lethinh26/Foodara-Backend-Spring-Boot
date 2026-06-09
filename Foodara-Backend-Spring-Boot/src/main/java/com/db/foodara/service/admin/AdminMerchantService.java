package com.db.foodara.service.admin;

import com.db.foodara.dto.request.admin.UpdateMerchantApprovalRequest;
import com.db.foodara.dto.request.admin.UpdateStoreStatusRequest;
import com.db.foodara.dto.request.admin.VerifyDocumentRequest;
import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.*;
import com.db.foodara.entity.merchant.*;
import com.db.foodara.entity.store.Store;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.*;
import com.db.foodara.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMerchantService {

    private final MerchantRepository merchantRepository;
    private final StoreRepository storeRepository;
    private final StoreDocumentRepository storeDocumentRepository;
    private final StoreBankAccountRepository storeBankAccountRepository;
    private final StoreOperatingHoursRepository storeOperatingHoursRepository;

    private static final Set<String> VALID_APPROVAL_STATUSES = Set.of("pending", "approved", "rejected", "suspended");
    private static final Set<String> VALID_VERIFICATION_STATUSES = Set.of("pending", "verified", "rejected");

    // --- Merchants ---

    public PageResponse<AdminMerchantResponse> getMerchants(int page, int size, String search, String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Merchant> merchantPage;

        if (search != null && !search.isBlank()) {
            merchantPage = merchantRepository.searchMerchants(search.trim(), pageRequest);
        } else if (status != null && !status.isBlank()) {
            merchantPage = merchantRepository.findByApprovalStatus(status, pageRequest);
        } else {
            merchantPage = merchantRepository.findAll(pageRequest);
        }

        List<AdminMerchantResponse> content = merchantPage.getContent().stream()
                .map(this::mapMerchantToResponse)
                .toList();

        return PageResponse.<AdminMerchantResponse>builder()
                .content(content)
                .page(merchantPage.getNumber())
                .number(merchantPage.getNumber())
                .size(merchantPage.getSize())
                .totalElements(merchantPage.getTotalElements())
                .totalPages(merchantPage.getTotalPages())
                .last(merchantPage.isLast())
                .build();
    }

    public AdminMerchantResponse getMerchantDetail(String merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        return mapMerchantToResponse(merchant);
    }

    @Transactional
    public void updateMerchantApproval(String merchantId, UpdateMerchantApprovalRequest request) {
        if (!VALID_APPROVAL_STATUSES.contains(request.getApprovalStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));

        merchant.setApprovalStatus(request.getApprovalStatus());
        merchantRepository.save(merchant);
    }

    public List<AdminStoreDocumentResponse> getMerchantDocuments(String merchantId) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new AppException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return storeDocumentRepository.findByMerchantId(merchantId).stream()
                .map(this::mapDocumentToResponse)
                .toList();
    }

    public List<AdminStoreBankAccountResponse> getMerchantBankAccounts(String merchantId) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new AppException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return storeBankAccountRepository.findByMerchantId(merchantId).stream()
                .map(this::mapBankAccountToResponse)
                .toList();
    }

    // --- Stores ---

    public PageResponse<AdminStoreResponse> getStores(int page, int size, String search, Boolean isActive) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Store> storePage;

        if (search != null && !search.isBlank()) {
            storePage = storeRepository.searchStores(search.trim(), pageRequest);
        } else if (isActive != null) {
            storePage = storeRepository.findByIsActive(isActive, pageRequest);
        } else {
            storePage = storeRepository.findAll(pageRequest);
        }

        List<AdminStoreResponse> content = storePage.getContent().stream()
                .map(this::mapStoreToResponse)
                .toList();

        return PageResponse.<AdminStoreResponse>builder()
                .content(content)
                .page(storePage.getNumber())
                .number(storePage.getNumber())
                .size(storePage.getSize())
                .totalElements(storePage.getTotalElements())
                .totalPages(storePage.getTotalPages())
                .last(storePage.isLast())
                .build();
    }

    public AdminStoreResponse getStoreDetail(String storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        return mapStoreToResponse(store);
    }

    @Transactional
    public void updateStoreStatus(String storeId, UpdateStoreStatusRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        store.setIsActive(request.getIsActive());
        storeRepository.save(store);
    }

    public List<AdminStoreOperatingHoursResponse> getStoreOperatingHours(String storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }
        return storeOperatingHoursRepository.findByStoreId(storeId).stream()
                .map(this::mapOperatingHoursToResponse)
                .toList();
    }

    public List<AdminStoreDocumentResponse> getStoreDocuments(String storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }
        return storeDocumentRepository.findByStoreId(storeId).stream()
                .map(this::mapDocumentToResponse)
                .toList();
    }

    // --- Documents ---

    @Transactional
    public void verifyDocument(String documentId, VerifyDocumentRequest request) {
        if (!VALID_VERIFICATION_STATUSES.contains(request.getVerificationStatus())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        StoreDocument document = storeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        document.setVerificationStatus(request.getVerificationStatus());
        document.setVerifiedAt(LocalDateTime.now());

        String adminId = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        document.setVerifiedBy(adminId);

        storeDocumentRepository.save(document);
    }

    // --- Mappers ---

    private AdminMerchantResponse mapMerchantToResponse(Merchant merchant) {
        int storeCount = storeRepository.countByMerchantId(merchant.getId());
        
        return AdminMerchantResponse.builder()
                .id(merchant.getId())
                .ownerId(merchant.getOwnerId())
                .name(merchant.getName())
                .taxCode(merchant.getTaxCode())
                .businessEmail(merchant.getBusinessEmail())
                .businessPhone(merchant.getBusinessPhone())
                .logoUrl(merchant.getLogoUrl())
                .coverImageUrl(merchant.getCoverImageUrl())
                .approvalStatus(merchant.getApprovalStatus())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .storeCount(storeCount)
                .build();
    }

    private AdminStoreResponse mapStoreToResponse(Store store) {
        return AdminStoreResponse.builder()
                .id(store.getId())
                .merchantId(store.getMerchantId())
                .name(store.getName())
                .slug(store.getSlug())
                .description(store.getDescription())
                .phone(store.getPhone())
                .addressLine(store.getAddressLine())
                .ward(store.getWard())
                .districtName(store.getDistrictName())
                .cityName(store.getCityName())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .isOpen(store.getIsOpen())
                .isActive(store.getIsActive())
                .autoAcceptOrders(store.getAutoAcceptOrders())
                .avgPreparationTime(store.getAvgPreparationTime())
                .minOrderAmount(store.getMinOrderAmount())
                .maxDeliveryRadiusKm(store.getMaxDeliveryRadiusKm())
                .avgRating(store.getAvgRating())
                .totalRatings(store.getTotalRatings())
                .totalOrders(store.getTotalOrders())
                .commissionRate(store.getCommissionRate())
                .coverImageUrl(store.getCoverImageUrl())
                .logoUrl(store.getLogoUrl())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }

    private AdminStoreDocumentResponse mapDocumentToResponse(StoreDocument doc) {
        return AdminStoreDocumentResponse.builder()
                .id(doc.getId())
                .merchantId(doc.getMerchantId())
                .storeId(doc.getStoreId())
                .documentType(doc.getDocumentType())
                .documentUrl(doc.getDocumentUrl())
                .documentNumber(doc.getDocumentNumber())
                .expiryDate(doc.getExpiryDate())
                .verificationStatus(doc.getVerificationStatus())
                .verifiedAt(doc.getVerifiedAt())
                .verifiedBy(doc.getVerifiedBy())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    private AdminStoreBankAccountResponse mapBankAccountToResponse(StoreBankAccount acc) {
        return AdminStoreBankAccountResponse.builder()
                .id(acc.getId())
                .merchantId(acc.getMerchantId())
                .bankName(acc.getBankName())
                .accountNumber(acc.getAccountNumber())
                .accountHolder(acc.getAccountHolder())
                .branch(acc.getBranch())
                .isDefault(acc.getIsDefault())
                .isVerified(acc.getIsVerified())
                .createdAt(acc.getCreatedAt())
                .updatedAt(acc.getUpdatedAt())
                .build();
    }

    private AdminStoreOperatingHoursResponse mapOperatingHoursToResponse(StoreOperatingHours oh) {
        return AdminStoreOperatingHoursResponse.builder()
                .id(oh.getId())
                .storeId(oh.getStoreId())
                .dayOfWeek(oh.getDayOfWeek())
                .openTime(oh.getOpenTime())
                .closeTime(oh.getCloseTime())
                .isClosed(oh.getIsClosed())
                .createdAt(oh.getCreatedAt())
                .updatedAt(oh.getUpdatedAt())
                .build();
    }
}
