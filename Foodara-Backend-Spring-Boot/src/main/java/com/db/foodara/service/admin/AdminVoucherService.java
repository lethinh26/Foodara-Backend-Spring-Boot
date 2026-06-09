package com.db.foodara.service.admin;

import com.db.foodara.dto.response.PageResponse;
import com.db.foodara.dto.response.admin.AdminVoucherResponse;
import com.db.foodara.entity.promotion.Voucher;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.promotion.VoucherRepository;
import com.db.foodara.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminVoucherService {

    private final VoucherRepository voucherRepository;
    private final StoreRepository storeRepository;

    public PageResponse<AdminVoucherResponse> getVouchers(int page, int size, String search, String type, Boolean isActive) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Voucher> voucherPage;

        if (search != null && !search.isBlank()) {
            voucherPage = voucherRepository.searchVouchers(search.trim(), pageRequest);
        } else if (type != null && !type.isBlank()) {
            voucherPage = voucherRepository.findByVoucherType(type, pageRequest);
        } else if (isActive != null) {
            voucherPage = voucherRepository.findByIsActive(isActive, pageRequest);
        } else {
            voucherPage = voucherRepository.findAll(pageRequest);
        }

        List<AdminVoucherResponse> content = voucherPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AdminVoucherResponse>builder()
                .content(content)
                .page(voucherPage.getNumber())
                .number(voucherPage.getNumber())
                .size(voucherPage.getSize())
                .totalElements(voucherPage.getTotalElements())
                .totalPages(voucherPage.getTotalPages())
                .last(voucherPage.isLast())
                .build();
    }

    @Transactional
    public AdminVoucherResponse createVoucher(Map<String, Object> request) {
        Voucher voucher = new Voucher();
        voucher.setVoucherType("platform"); // Admin only creates platform vouchers
        applyVoucherFields(voucher, request);
        voucherRepository.save(voucher);
        return mapToResponse(voucher);
    }

    @Transactional
    public void updateVoucher(String id, Map<String, Object> request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        applyVoucherFields(voucher, request);
        voucherRepository.save(voucher);
    }

    @Transactional
    public void deleteVoucher(String id) {
        if (!voucherRepository.existsById(id)) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND);
        }
        voucherRepository.deleteById(id);
    }

    private void applyVoucherFields(Voucher v, Map<String, Object> data) {
        if (data.containsKey("code")) v.setCode((String) data.get("code"));
        if (data.containsKey("title")) v.setTitle((String) data.get("title"));
        if (data.containsKey("description")) v.setDescription((String) data.get("description"));
        if (data.containsKey("discountType")) v.setDiscountType((String) data.get("discountType"));
        if (data.containsKey("discountValue")) v.setDiscountValue(toBigDecimal(data.get("discountValue")));
        if (data.containsKey("minOrderValue")) v.setMinOrderValue(toBigDecimal(data.get("minOrderValue")));
        if (data.containsKey("maxDiscountValue")) v.setMaxDiscountValue(toBigDecimal(data.get("maxDiscountValue")));
        if (data.containsKey("totalQuantity")) v.setTotalQuantity(toInteger(data.get("totalQuantity")));
        if (data.containsKey("userUsageLimit")) v.setUserUsageLimit(toInteger(data.get("userUsageLimit")));
        if (data.containsKey("isStackable")) v.setIsStackable((Boolean) data.get("isStackable"));
        if (data.containsKey("applicableTo")) v.setApplicableTo((String) data.get("applicableTo"));
        if (data.containsKey("startsAt")) v.setStartsAt(toDateTime(data.get("startsAt")));
        if (data.containsKey("expiresAt")) v.setExpiresAt(toDateTime(data.get("expiresAt")));
        if (data.containsKey("isActive")) v.setIsActive((Boolean) data.get("isActive"));
        if (data.containsKey("campaignId")) v.setCampaignId((String) data.get("campaignId"));
    }

    private AdminVoucherResponse mapToResponse(Voucher v) {
        // Enrich store name if store voucher
        String storeName = null;
        if (v.getStoreId() != null) {
            storeName = storeRepository.findById(v.getStoreId())
                    .map(s -> s.getName())
                    .orElse(null);
        }

        return AdminVoucherResponse.builder()
                .id(v.getId())
                .voucherType(v.getVoucherType())
                .campaignId(v.getCampaignId())
                .merchantId(v.getMerchantId())
                .storeId(v.getStoreId())
                .storeName(storeName)
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
                .createdAt(v.getCreatedAt())
                .build();
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(val.toString());
    }

    private Integer toInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.intValue();
        return Integer.parseInt(val.toString());
    }

    private LocalDateTime toDateTime(Object val) {
        if (val == null) return null;
        return LocalDateTime.parse(val.toString());
    }
}
