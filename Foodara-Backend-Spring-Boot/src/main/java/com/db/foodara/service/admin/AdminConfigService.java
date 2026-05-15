package com.db.foodara.service.admin;

import com.db.foodara.entity.config.DeliveryFeeConfig;
import com.db.foodara.entity.config.PlatformConfig;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.config.DeliveryFeeConfigRepository;
import com.db.foodara.repository.config.PlatformConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminConfigService {

    private final PlatformConfigRepository platformConfigRepo;
    private final DeliveryFeeConfigRepository deliveryFeeConfigRepo;

    public List<PlatformConfig> getPlatformConfigs() {
        return platformConfigRepo.findAll();
    }

    @Transactional
    public void updatePlatformConfig(String key, String configValue, String adminUserId) {
        PlatformConfig config = platformConfigRepo.findByConfigKey(key)
                .orElseThrow(() -> new AppException(ErrorCode.CONFIG_NOT_FOUND));

        if (!Boolean.TRUE.equals(config.getIsEditable())) {
            throw new AppException(ErrorCode.CONFIG_NOT_EDITABLE);
        }

        config.setConfigValue(configValue);
        config.setUpdatedBy(adminUserId);
        platformConfigRepo.save(config);

        log.info("Admin {} updated platform config '{}' to '{}'", adminUserId, key, configValue);
    }

    public List<DeliveryFeeConfig> getDeliveryFeeConfigs() {
        return deliveryFeeConfigRepo.findAll();
    }

    @Transactional
    public void updateDeliveryFeeConfig(String id, Map<String, Object> data) {
        DeliveryFeeConfig config = deliveryFeeConfigRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONFIG_NOT_FOUND));

        if (data.containsKey("baseFee")) config.setBaseFee(toBigDecimal(data.get("baseFee")));
        if (data.containsKey("baseDistanceKm")) config.setBaseDistanceKm(toBigDecimal(data.get("baseDistanceKm")));
        if (data.containsKey("perKmFee")) config.setPerKmFee(toBigDecimal(data.get("perKmFee")));
        if (data.containsKey("surgeEnabled")) config.setSurgeEnabled((Boolean) data.get("surgeEnabled"));
        if (data.containsKey("surgeMultiplier")) config.setSurgeMultiplier(toBigDecimal(data.get("surgeMultiplier")));
        if (data.containsKey("surgeStartTime")) config.setSurgeStartTime(toLocalTime(data.get("surgeStartTime")));
        if (data.containsKey("surgeEndTime")) config.setSurgeEndTime(toLocalTime(data.get("surgeEndTime")));
        if (data.containsKey("rainSurgeMultiplier")) config.setRainSurgeMultiplier(toBigDecimal(data.get("rainSurgeMultiplier")));
        if (data.containsKey("isActive")) config.setIsActive((Boolean) data.get("isActive"));

        deliveryFeeConfigRepo.save(config);
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(val.toString());
    }

    private LocalTime toLocalTime(Object val) {
        if (val == null) return null;
        return LocalTime.parse(val.toString());
    }
}
