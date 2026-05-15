package com.db.foodara.entity.settlement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_settlement_items")
@Getter
@Setter
public class StoreSettlementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "settlement_id", nullable = false)
    private String settlementId;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "order_subtotal", precision = 12, scale = 2)
    private BigDecimal orderSubtotal;

    @Column(name = "commission_amount", precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "voucher_subsidy", precision = 12, scale = 2)
    private BigDecimal voucherSubsidy;

    @Column(name = "deduction", precision = 12, scale = 2)
    private BigDecimal deduction;

    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
