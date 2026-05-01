package com.db.foodara.dto.response.order;

import com.db.foodara.dto.response.promotion.VoucherPricingResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {

    private String id;
    private String userId;
    private String storeId;
    private String storeName;
    private BigDecimal storeMinOrderAmount;
    private Boolean isStoreOpen;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal subtotalAfterVoucher;
    private BigDecimal totalVoucherDiscount;
    private VoucherPricingResponse bestPlatformVoucher;
    private VoucherPricingResponse bestStoreVoucher;
    private LocalDateTime updatedAt;
    private List<CartItemResponse> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CartItemResponse {
        private String id;
        private String menuItemId;
        private String comboId;
        private String name;
        private String imageUrl;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private BigDecimal discountedUnitPrice;
        private BigDecimal discountedTotalPrice;
        private String specialInstructions;
        private List<CartItemOptionResponse> options;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CartItemOptionResponse {
        private String id;
        private String optionItemId;
        private String optionGroupId;
        private String optionGroupName;
        private String optionName;
        private BigDecimal priceAdjustment;
        private Boolean isSize;
    }
}
