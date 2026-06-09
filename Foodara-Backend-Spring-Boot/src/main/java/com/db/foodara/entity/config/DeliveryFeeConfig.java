package com.db.foodara.entity.config;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "delivery_fee_configs")
@Getter
@Setter
public class DeliveryFeeConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "base_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFee;

    @Column(name = "base_distance_km", nullable = false, precision = 5, scale = 2)
    private BigDecimal baseDistanceKm;

    @Column(name = "per_km_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal perKmFee;

    @Column(name = "surge_enabled")
    private Boolean surgeEnabled;

    @Column(name = "surge_multiplier", precision = 3, scale = 2)
    private BigDecimal surgeMultiplier;

    @Column(name = "surge_start_time")
    private LocalTime surgeStartTime;

    @Column(name = "surge_end_time")
    private LocalTime surgeEndTime;

    @Column(name = "rain_surge_multiplier", precision = 3, scale = 2)
    private BigDecimal rainSurgeMultiplier;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (surgeEnabled == null) surgeEnabled = false;
        if (surgeMultiplier == null) surgeMultiplier = BigDecimal.ONE;
        if (rainSurgeMultiplier == null) rainSurgeMultiplier = BigDecimal.ONE;
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
