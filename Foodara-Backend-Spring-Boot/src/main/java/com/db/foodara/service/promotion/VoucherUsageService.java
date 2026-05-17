package com.db.foodara.service.promotion;

import com.db.foodara.entity.promotion.VoucherUsage;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.promotion.UserVoucherRepository;
import com.db.foodara.repository.promotion.VoucherRepository;
import com.db.foodara.repository.promotion.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherUsageService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final UserVoucherRepository userVoucherRepository;


    @Transactional
    public void recordUsage(String orderId, String userId, String voucherId, BigDecimal discountAmount) {
        if (!StringUtils.hasText(voucherId)) {
            return;
        }

        int updated = voucherRepository.incrementUsedQuantity(voucherId);
        if (updated == 0) {
            // The voucher reached its limit between preview and place-order — roll the order back.
            log.warn("Voucher {} sold out at order {}", voucherId, orderId);
            throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
        }

        VoucherUsage usage = new VoucherUsage();
        usage.setVoucherId(voucherId);
        usage.setUserId(userId);
        usage.setOrderId(orderId);
        usage.setDiscountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        voucherUsageRepository.save(usage);

        userVoucherRepository.markUsed(userId, voucherId, orderId, LocalDateTime.now());
        log.debug("Recorded voucher usage voucher={} user={} order={}", voucherId, userId, orderId);
    }


    @Transactional
    public void rollbackForOrder(String orderId) {
        if (!StringUtils.hasText(orderId)) return;

        var usages = voucherUsageRepository.findByOrderId(orderId);
        for (VoucherUsage usage : usages) {
            voucherRepository.decrementUsedQuantity(usage.getVoucherId());
        }
        userVoucherRepository.resetUsageForOrder(orderId);
        voucherUsageRepository.deleteByOrderId(orderId);
        log.debug("Rolled back voucher usage for order {} ({} usage rows)", orderId, usages.size());
    }
}
