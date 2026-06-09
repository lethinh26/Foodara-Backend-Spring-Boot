package com.db.foodara.dto.request.merchant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for merchant creating/updating a store voucher.
 * Merchant can only manage `store` type vouchers; `merchantId` and
 * `storeId` are derived from the authenticated merchant context.
 */
@Getter
@Setter
public class MerchantVoucherRequest {

    @NotBlank(message = "Voucher code is required")
    @Size(max = 50, message = "Voucher code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Voucher title is required")
    @Size(max = 255, message = "Voucher title must not exceed 255 characters")
    private String title;

    private String description;

    /** percentage | fixed | free_ship */
    @NotBlank(message = "Discount type is required")
    private String discountType;

    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;

    private Integer totalQuantity;
    private Integer userUsageLimit;

    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;

    private Boolean isActive;
}
