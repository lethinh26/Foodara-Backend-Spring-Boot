package com.db.foodara.dto.response.store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.db.foodara.dto.response.promotion.VoucherPricingResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItemResponse {

    private String id;
    private String storeId;
    private String categoryId;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private BigDecimal estimatedDiscountAmount;
    private VoucherPricingResponse bestVoucher;
    private Boolean isAvailable;
    private Boolean isActive;
    private Boolean isPopular;
    private Boolean isNew;
    private Integer displayOrder;
    private BigDecimal avgRating;
    private Integer totalRatings;
    private Integer totalSold;
    private Integer maxQuantityPerOrder;
    private Boolean trackInventory;
    private Integer stockQuantity;
    private Integer dailyLimit;
    private Integer dailySoldCount;
    private LocalDateTime createdAt;
}