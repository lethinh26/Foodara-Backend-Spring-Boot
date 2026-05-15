package com.db.foodara.entity.settlement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_settlements")
@Getter
@Setter
public class DriverSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries;

    @Column(name = "total_delivery_earnings", precision = 14, scale = 2)
    private BigDecimal totalDeliveryEarnings;

    @Column(name = "total_tips", precision = 14, scale = 2)
    private BigDecimal totalTips;

    @Column(name = "total_bonuses", precision = 14, scale = 2)
    private BigDecimal totalBonuses;

    @Column(name = "total_cod_collected", precision = 14, scale = 2)
    private BigDecimal totalCodCollected;

    @Column(name = "total_cod_transferred", precision = 14, scale = 2)
    private BigDecimal totalCodTransferred;

    @Column(name = "total_deductions", precision = 14, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "net_amount", precision = 14, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = "pending";
        if (totalDeliveries == null) totalDeliveries = 0;
        if (totalDeliveryEarnings == null) totalDeliveryEarnings = BigDecimal.ZERO;
        if (totalTips == null) totalTips = BigDecimal.ZERO;
        if (totalBonuses == null) totalBonuses = BigDecimal.ZERO;
        if (totalCodCollected == null) totalCodCollected = BigDecimal.ZERO;
        if (totalCodTransferred == null) totalCodTransferred = BigDecimal.ZERO;
        if (totalDeductions == null) totalDeductions = BigDecimal.ZERO;
        if (netAmount == null) netAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
