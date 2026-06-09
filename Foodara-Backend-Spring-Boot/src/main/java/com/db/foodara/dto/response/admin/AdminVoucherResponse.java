package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminVoucherResponse {
    private String id;
    private String voucherType;
    private String campaignId;
    private String merchantId;
    private String storeId;
    private String storeName;
    private String code;
    private String title;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private Integer totalQuantity;
    private Integer usedQuantity;
    private Integer userUsageLimit;
    private Boolean isStackable;
    private String applicableTo;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
