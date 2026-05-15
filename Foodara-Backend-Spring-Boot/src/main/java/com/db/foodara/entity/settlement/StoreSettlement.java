package com.db.foodara.entity.settlement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_settlements")
@Getter
@Setter
public class StoreSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "total_gmv", precision = 14, scale = 2)
    private BigDecimal totalGmv;

    @Column(name = "total_commission", precision = 14, scale = 2)
    private BigDecimal totalCommission;

    @Column(name = "total_voucher_subsidy", precision = 14, scale = 2)
    private BigDecimal totalVoucherSubsidy;

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

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "confirmed_by")
    private String confirmedBy;

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
        if (totalOrders == null) totalOrders = 0;
        if (totalGmv == null) totalGmv = BigDecimal.ZERO;
        if (totalCommission == null) totalCommission = BigDecimal.ZERO;
        if (totalVoucherSubsidy == null) totalVoucherSubsidy = BigDecimal.ZERO;
        if (totalDeductions == null) totalDeductions = BigDecimal.ZERO;
        if (netAmount == null) netAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
