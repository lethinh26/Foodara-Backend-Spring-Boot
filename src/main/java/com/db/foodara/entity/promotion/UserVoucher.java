package com.db.foodara.entity.promotion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_vouchers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "voucher_id"})
)
@Getter
@Setter
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "is_used")
    private Boolean isUsed;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        collectedAt = LocalDateTime.now();
        if (isUsed == null) isUsed = false;
    }
}
