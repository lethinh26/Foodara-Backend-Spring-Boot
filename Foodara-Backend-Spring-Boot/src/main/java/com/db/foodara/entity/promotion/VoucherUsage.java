package com.db.foodara.entity.promotion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "voucher_usage")
@Getter
@Setter
public class VoucherUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "voucher_id", nullable = false)
    private String voucherId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        if (usedAt == null) usedAt = LocalDateTime.now();
    }
}
