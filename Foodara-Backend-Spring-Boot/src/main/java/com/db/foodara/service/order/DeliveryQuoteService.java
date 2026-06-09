package com.db.foodara.service.order;

import com.db.foodara.entity.config.DeliveryFeeConfig;
import com.db.foodara.entity.store.Store;
import com.db.foodara.entity.user.UserAddress;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.dto.internal.mapbox.DirectionsResult;
import com.db.foodara.repository.config.DeliveryFeeConfigRepository;
import com.db.foodara.repository.store.StoreRepository;
import com.db.foodara.repository.user.UserAddressRepository;
import com.db.foodara.service.location.mapbox.MapboxClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryQuoteService {

    private static final BigDecimal DEFAULT_BASE_FEE = BigDecimal.valueOf(15000);
    private static final BigDecimal DEFAULT_BASE_DISTANCE_KM = BigDecimal.valueOf(2);
    private static final BigDecimal DEFAULT_PER_KM_FEE = BigDecimal.valueOf(3000);
    private static final BigDecimal DEFAULT_MAX_FEE = BigDecimal.valueOf(60000);
    private static final double DEFAULT_AVG_SPEED_KMH = 25.0;

    private final StoreRepository storeRepository;
    private final UserAddressRepository userAddressRepository;
    private final DeliveryFeeConfigRepository deliveryFeeConfigRepository;
    private final MapboxClient mapboxClient;

    public DeliveryQuote quote(String storeId, String userId, String addressId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        UserAddress address = resolveAddress(userId, addressId);

        if (address == null
                || address.getLatitude() == null || address.getLongitude() == null
                || store.getLatitude() == null || store.getLongitude() == null) {
            DeliveryFeeConfig cfg = activeConfig();
            BigDecimal fallbackFee = cfg != null ? safe(cfg.getBaseFee(), DEFAULT_BASE_FEE) : DEFAULT_BASE_FEE;
            return new DeliveryQuote(
                    store.getId(),
                    address != null ? address.getId() : null,
                    BigDecimal.ZERO,
                    null,
                    scale(fallbackFee),
                    BigDecimal.ONE);
        }

        BigDecimal distanceKm;
        Integer etaMinutes;
        try {
            DirectionsResult dir = mapboxClient.directions(
                    store.getLatitude().doubleValue(), store.getLongitude().doubleValue(),
                    address.getLatitude().doubleValue(), address.getLongitude().doubleValue(),
                    null);
            distanceKm = dir.distanceKm();
            etaMinutes = dir.durationMinutes();
        } catch (AppException ex) {
            log.warn("Mapbox directions failed [{}], fallback Haversine", ex.getErrorCode());
            distanceKm = haversineKm(
                    store.getLatitude().doubleValue(), store.getLongitude().doubleValue(),
                    address.getLatitude().doubleValue(), address.getLongitude().doubleValue());
            etaMinutes = (int) Math.ceil(distanceKm.doubleValue() / DEFAULT_AVG_SPEED_KMH * 60d);
        }

        DeliveryFeeConfig cfg = activeConfig();
        BigDecimal baseFee = cfg != null ? safe(cfg.getBaseFee(), DEFAULT_BASE_FEE) : DEFAULT_BASE_FEE;
        BigDecimal baseDistance = cfg != null ? safe(cfg.getBaseDistanceKm(), DEFAULT_BASE_DISTANCE_KM) : DEFAULT_BASE_DISTANCE_KM;
        BigDecimal perKm = cfg != null ? safe(cfg.getPerKmFee(), DEFAULT_PER_KM_FEE) : DEFAULT_PER_KM_FEE;

        BigDecimal extra = distanceKm.subtract(baseDistance).max(BigDecimal.ZERO);
        BigDecimal fee = baseFee.add(extra.multiply(perKm));

        BigDecimal multiplier = resolveSurge(cfg);
        if (multiplier.compareTo(BigDecimal.ONE) > 0) {
            fee = fee.multiply(multiplier);
        }
        if (fee.compareTo(DEFAULT_MAX_FEE) > 0) {
            fee = DEFAULT_MAX_FEE;
        }

        return new DeliveryQuote(
                store.getId(),
                address.getId(),
                scale(distanceKm),
                etaMinutes,
                scale(fee),
                multiplier);
    }

    public DeliveryQuote quoteByCoords(String storeId, BigDecimal fromLat, BigDecimal fromLng) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        if (fromLat == null || fromLng == null
                || store.getLatitude() == null || store.getLongitude() == null) {
            throw new AppException(ErrorCode.INVALID_COORDINATES);
        }
        BigDecimal distanceKm;
        Integer etaMinutes;
        try {
            DirectionsResult dir = mapboxClient.directions(
                    store.getLatitude().doubleValue(), store.getLongitude().doubleValue(),
                    fromLat.doubleValue(), fromLng.doubleValue(), null);
            distanceKm = dir.distanceKm();
            etaMinutes = dir.durationMinutes();
        } catch (AppException ex) {
            log.warn("Mapbox directions failed [{}], fallback Haversine", ex.getErrorCode());
            distanceKm = haversineKm(
                    store.getLatitude().doubleValue(), store.getLongitude().doubleValue(),
                    fromLat.doubleValue(), fromLng.doubleValue());
            etaMinutes = (int) Math.ceil(distanceKm.doubleValue() / DEFAULT_AVG_SPEED_KMH * 60d);
        }
        DeliveryFeeConfig cfg = activeConfig();
        BigDecimal baseFee = cfg != null ? safe(cfg.getBaseFee(), DEFAULT_BASE_FEE) : DEFAULT_BASE_FEE;
        BigDecimal baseDistance = cfg != null ? safe(cfg.getBaseDistanceKm(), DEFAULT_BASE_DISTANCE_KM) : DEFAULT_BASE_DISTANCE_KM;
        BigDecimal perKm = cfg != null ? safe(cfg.getPerKmFee(), DEFAULT_PER_KM_FEE) : DEFAULT_PER_KM_FEE;
        BigDecimal extra = distanceKm.subtract(baseDistance).max(BigDecimal.ZERO);
        BigDecimal fee = baseFee.add(extra.multiply(perKm));
        BigDecimal multiplier = resolveSurge(cfg);
        if (multiplier.compareTo(BigDecimal.ONE) > 0) fee = fee.multiply(multiplier);
        if (fee.compareTo(DEFAULT_MAX_FEE) > 0) fee = DEFAULT_MAX_FEE;
        return new DeliveryQuote(store.getId(), null, scale(distanceKm), etaMinutes, scale(fee), multiplier);
    }

    public java.util.List<DeliveryQuote> quoteBatch(java.util.List<String> storeIds, BigDecimal lat, BigDecimal lng) {
        if (storeIds == null || storeIds.isEmpty()) return java.util.List.of();
        return storeIds.stream().map(id -> {
            try { return quoteByCoords(id, lat, lng); }
            catch (AppException ex) { log.warn("quoteBatch fail store={} code={}", id, ex.getErrorCode()); return null; }
        }).toList();
    }

    private DeliveryFeeConfig activeConfig() {
        return deliveryFeeConfigRepository
                .findFirstByIsActiveTrueOrderByUpdatedAtDesc()
                .orElseGet(() -> {
                    log.warn("No active DeliveryFeeConfig; using defaults");
                    return null;
                });
    }

    private BigDecimal resolveSurge(DeliveryFeeConfig cfg) {
        if (cfg == null || !Boolean.TRUE.equals(cfg.getSurgeEnabled())) return BigDecimal.ONE;
        BigDecimal mult = cfg.getSurgeMultiplier();
        if (mult == null || mult.compareTo(BigDecimal.ONE) <= 0) return BigDecimal.ONE;
        if (cfg.getSurgeStartTime() == null || cfg.getSurgeEndTime() == null) return BigDecimal.ONE;
        LocalTime now = LocalTime.now();
        LocalTime start = cfg.getSurgeStartTime();
        LocalTime end = cfg.getSurgeEndTime();
        boolean inWindow = start.isBefore(end)
                ? (!now.isBefore(start) && now.isBefore(end))
                : (!now.isBefore(start) || now.isBefore(end));
        return inWindow ? mult : BigDecimal.ONE;
    }

    private UserAddress resolveAddress(String userId, String addressId) {
        if (StringUtils.hasText(addressId)) {
            return userAddressRepository.findByIdAndUserId(addressId, userId)
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        }
        return userAddressRepository.findByUserIdAndIsDefaultTrue(userId).orElse(null);
    }

    private BigDecimal haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(earthRadius * c).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : fallback;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record DeliveryQuote(
            String storeId,
            String addressId,
            BigDecimal distanceKm,
            Integer etaMinutes,
            BigDecimal deliveryFee,
            BigDecimal surgeMultiplier
    ) {}
}
