package com.db.foodara.driver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_shifts")
@Getter
@Setter
public class DriverShift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "went_online_at", nullable = false)
    private LocalDateTime wentOnlineAt;

    @Column(name = "went_offline_at")
    private LocalDateTime wentOfflineAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_earnings")
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
