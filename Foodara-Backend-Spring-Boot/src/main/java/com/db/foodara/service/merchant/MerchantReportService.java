package com.db.foodara.service.merchant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.db.foodara.dto.response.merchant.MerchantRevenuePoint;
import com.db.foodara.entity.merchant.Merchant;
import com.db.foodara.entity.store.Store;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.merchant.MerchantRepository;
import com.db.foodara.repository.order.OrderRepository;
import com.db.foodara.repository.store.StoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * M09 — Merchant store-level analytics.
 * Calculates totals and time-bucketed revenue series for a store
 * owned by the authenticated merchant.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MerchantRepository merchantRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    public BigDecimal totalRevenue(String userId, String storeId) {
        Store store = ensureStoreOwned(userId, storeId);
        BigDecimal revenue = orderRepository.sumRevenueByStore(store.getId());
        return revenue != null ? revenue.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public Long totalOrders(String userId, String storeId) {
        Store store = ensureStoreOwned(userId, storeId);
        return orderRepository.countOrdersByStore(store.getId());
    }

    public Integer avgPreparationMinutes(String userId, String storeId) {
        Store store = ensureStoreOwned(userId, storeId);
        Double avg = orderRepository.avgPreparationMinutesByStore(store.getId());
        if (avg == null || avg <= 0d) {
            Integer fallback = store.getAvgPreparationTime();
            return fallback != null ? fallback : 0;
        }
        return (int) Math.round(avg);
    }

    public BigDecimal successRatePercent(String userId, String storeId) {
        Store store = ensureStoreOwned(userId, storeId);
        long total = orderRepository.countOrdersByStore(store.getId());
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        long completed = orderRepository.countCompletedOrdersByStore(store.getId());
        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    public List<MerchantRevenuePoint> revenueData(String userId, String storeId, String startDate, String endDate) {
        Store store = ensureStoreOwned(userId, storeId);

        LocalDate start;
        LocalDate end;
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            start = LocalDate.parse(startDate, DATE_FMT);
            end = LocalDate.parse(endDate, DATE_FMT);
            if (end.isBefore(start)) {
                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
            }
        } else {
            // Default to current week (Mon..Sun)
            LocalDate today = LocalDate.now();
            start = today.with(DayOfWeek.MONDAY);
            end = start.plusDays(6);
        }

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.plusDays(1).atStartOfDay();

        Map<String, Object[]> rowsByDate = new HashMap<>();
        for (Object[] row : orderRepository.findDailyRevenueByStore(store.getId(), startDt, endDt)) {
            rowsByDate.put((String) row[0], row);
        }

        List<MerchantRevenuePoint> points = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String key = d.format(DATE_FMT);
            Object[] row = rowsByDate.get(key);
            BigDecimal revenue = BigDecimal.ZERO;
            long orders = 0L;
            if (row != null) {
                if (row[1] instanceof Number n) {
                    revenue = BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP);
                }
                if (row[2] instanceof Number n) {
                    orders = n.longValue();
                }
            }
            points.add(MerchantRevenuePoint.builder()
                    .date(key)
                    .day(toShortDayLabel(d))
                    .revenue(revenue)
                    .orders(orders)
                    .build());
        }
        return points;
    }

    private Store ensureStoreOwned(String userId, String storeId) {
        Merchant merchant = merchantRepository.findByOwnerId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MERCHANT_NOT_FOUND));
        return storeRepository.findByIdAndMerchantId(storeId, merchant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
    }

    /** Vietnamese short day label: T2..T7, CN. */
    private String toShortDayLabel(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "T2";
            case TUESDAY -> "T3";
            case WEDNESDAY -> "T4";
            case THURSDAY -> "T5";
            case FRIDAY -> "T6";
            case SATURDAY -> "T7";
            case SUNDAY -> "CN";
        };
    }
}
