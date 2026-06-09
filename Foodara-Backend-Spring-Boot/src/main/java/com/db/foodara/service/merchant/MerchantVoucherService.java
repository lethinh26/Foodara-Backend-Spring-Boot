package com.db.foodara.service.merchant;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.db.foodara.dto.request.merchant.MerchantCampaignJoinRequest;
import com.db.foodara.dto.request.merchant.MerchantVoucherRequest;
import com.db.foodara.dto.response.merchant.MerchantCampaignJoinResponse;
import com.db.foodara.dto.response.promotion.VoucherResponse;
import com.db.foodara.entity.home.Campaign;
import com.db.foodara.entity.merchant.Merchant;
import com.db.foodara.entity.promotion.CampaignParticipant;
import com.db.foodara.entity.promotion.Voucher;
import com.db.foodara.entity.store.Store;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.home.CampaignRepository;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.repository.promotion.CampaignParticipantRepository;
import com.db.foodara.repository.promotion.VoucherRepository;
import com.db.foodara.repository.store.StoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * M08 — Merchant promotions management.
 * Handles store voucher CRUD and platform campaign participation
 * scoped to the authenticated merchant's stores.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantVoucherService {

    private final MerchantRepository merchantRepository;
    private final StoreRepository storeRepository;
    private final VoucherRepository voucherRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignParticipantRepository campaignParticipantRepository;

    // ---------- Vouchers ----------

    public List<VoucherResponse> getVouchers(String userId) {
        Merchant merchant = getMerchant(userId);
        return voucherRepository.findByMerchantIdOrderByCreatedAtDesc(merchant.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public VoucherResponse createVoucher(String userId, String storeId, MerchantVoucherRequest request) {
        Merchant merchant = getMerchant(userId);
        Store store = getStoreOwnedByMerchant(merchant, storeId);

        validateDateRange(request.getStartsAt(), request.getExpiresAt());

        String code = request.getCode().trim().toUpperCase();
        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new AppException(ErrorCode.VOUCHER_CODE_EXISTED);
        }

        Voucher voucher = new Voucher();
        voucher.setVoucherType("store");
        voucher.setMerchantId(merchant.getId());
        voucher.setStoreId(store.getId());
        voucher.setCode(code);
        applyEditableFields(voucher, request);

        Voucher saved = voucherRepository.save(voucher);
        return mapToResponse(saved);
    }

    @Transactional
    public VoucherResponse updateVoucher(String userId, String voucherId, MerchantVoucherRequest request) {
        Merchant merchant = getMerchant(userId);
        Voucher voucher = getVoucherOwnedByMerchant(merchant, voucherId);

        validateDateRange(request.getStartsAt(), request.getExpiresAt());

        if (StringUtils.hasText(request.getCode())) {
            String newCode = request.getCode().trim().toUpperCase();
            if (!newCode.equalsIgnoreCase(voucher.getCode())
                    && voucherRepository.existsByCodeIgnoreCase(newCode)) {
                throw new AppException(ErrorCode.VOUCHER_CODE_EXISTED);
            }
            voucher.setCode(newCode);
        }
        applyEditableFields(voucher, request);

        Voucher updated = voucherRepository.save(voucher);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteVoucher(String userId, String voucherId) {
        Merchant merchant = getMerchant(userId);
        Voucher voucher = getVoucherOwnedByMerchant(merchant, voucherId);
        voucherRepository.delete(voucher);
    }

    // ---------- Campaign participation ----------

    public List<MerchantCampaignJoinResponse> getJoinedCampaigns(String userId) {
        Merchant merchant = getMerchant(userId);
        List<String> storeIds = storeRepository.findByMerchantId(merchant.getId()).stream()
                .map(Store::getId)
                .toList();
        if (storeIds.isEmpty()) {
            return List.of();
        }
        return campaignParticipantRepository.findByStoreIdInOrderByJoinedAtDesc(storeIds).stream()
                .map(this::mapToCampaignJoinResponse)
                .toList();
    }

    @Transactional
    public MerchantCampaignJoinResponse joinCampaign(String userId, MerchantCampaignJoinRequest request) {
        Merchant merchant = getMerchant(userId);
        Store store = getStoreOwnedByMerchant(merchant, request.getStoreId());

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new AppException(ErrorCode.CAMPAIGN_NOT_FOUND));

        if (campaignParticipantRepository.existsByCampaignIdAndStoreId(campaign.getId(), store.getId())) {
            throw new AppException(ErrorCode.CAMPAIGN_ALREADY_JOINED);
        }

        CampaignParticipant participant = new CampaignParticipant();
        participant.setCampaignId(campaign.getId());
        participant.setStoreId(store.getId());
        participant.setStatus("active");
        participant.setEndedAt(campaign.getEndsAt());

        return mapToCampaignJoinResponse(campaignParticipantRepository.save(participant));
    }

    // ---------- Helpers ----------

    private Merchant getMerchant(String userId) {
        return merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
    }

    private Store getStoreOwnedByMerchant(Merchant merchant, String storeId) {
        return storeRepository.findByIdAndMerchantId(storeId, merchant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
    }

    private Voucher getVoucherOwnedByMerchant(Merchant merchant, String voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        if (voucher.getMerchantId() == null || !voucher.getMerchantId().equals(merchant.getId())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_OWNED_BY_MERCHANT);
        }
        return voucher;
    }

    private void applyEditableFields(Voucher voucher, MerchantVoucherRequest request) {
        if (request.getTitle() != null) voucher.setTitle(request.getTitle());
        if (request.getDescription() != null) voucher.setDescription(request.getDescription());
        if (request.getDiscountType() != null) voucher.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null) voucher.setDiscountValue(request.getDiscountValue());
        if (request.getMinOrderValue() != null) voucher.setMinOrderValue(request.getMinOrderValue());
        if (request.getMaxDiscountValue() != null) voucher.setMaxDiscountValue(request.getMaxDiscountValue());
        if (request.getTotalQuantity() != null) voucher.setTotalQuantity(request.getTotalQuantity());
        if (request.getUserUsageLimit() != null) voucher.setUserUsageLimit(request.getUserUsageLimit());
        if (request.getStartsAt() != null) voucher.setStartsAt(request.getStartsAt());
        if (request.getExpiresAt() != null) voucher.setExpiresAt(request.getExpiresAt());
        if (request.getIsActive() != null) voucher.setIsActive(request.getIsActive());
    }

    private void validateDateRange(LocalDateTime startsAt, LocalDateTime expiresAt) {
        if (startsAt != null && expiresAt != null && expiresAt.isBefore(startsAt)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
    }

    private VoucherResponse mapToResponse(Voucher v) {
        return VoucherResponse.builder()
                .id(v.getId())
                .voucherType(v.getVoucherType())
                .campaignId(v.getCampaignId())
                .merchantId(v.getMerchantId())
                .storeId(v.getStoreId())
                .code(v.getCode())
                .title(v.getTitle())
                .description(v.getDescription())
                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .maxDiscountValue(v.getMaxDiscountValue())
                .totalQuantity(v.getTotalQuantity())
                .usedQuantity(v.getUsedQuantity())
                .userUsageLimit(v.getUserUsageLimit())
                .isStackable(v.getIsStackable())
                .applicableTo(v.getApplicableTo())
                .startsAt(v.getStartsAt())
                .expiresAt(v.getExpiresAt())
                .isActive(v.getIsActive())
                .build();
    }

    private MerchantCampaignJoinResponse mapToCampaignJoinResponse(CampaignParticipant p) {
        return MerchantCampaignJoinResponse.builder()
                .id(p.getId())
                .campaignId(p.getCampaignId())
                .storeId(p.getStoreId())
                .status(p.getStatus())
                .joinedAt(p.getJoinedAt())
                .endedAt(p.getEndedAt())
                .build();
    }
}
