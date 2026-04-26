package com.db.foodara.entity.promotion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "voucher_type")
    private String voucherType;

    @Column(name = "campaign_id")
    private String campaignId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_order_value", precision = 12, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "max_discount_value", precision = 12, scale = 2)
    private BigDecimal maxDiscountValue;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "used_quantity")
    private Integer usedQuantity;

    @Column(name = "user_usage_limit")
    private Integer userUsageLimit;

    @Column(name = "is_stackable")
    private Boolean isStackable;

    @Column(name = "applicable_to")
    private String applicableTo;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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
        if (voucherType == null) voucherType = "platform";
        if (minOrderValue == null) minOrderValue = BigDecimal.ZERO;
        if (usedQuantity == null) usedQuantity = 0;
        if (userUsageLimit == null) userUsageLimit = 1;
        if (isStackable == null) isStackable = false;
        if (applicableTo == null) applicableTo = "all";
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
