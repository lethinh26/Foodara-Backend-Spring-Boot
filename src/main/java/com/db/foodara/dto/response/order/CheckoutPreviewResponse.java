package com.db.foodara.dto.response.order;

import com.db.foodara.dto.response.promotion.VoucherPricingResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutPreviewResponse {

    private String storeId;
    private String addressId;

    private BigDecimal subtotal;
    private BigDecimal subtotalAfterVoucher;
    private BigDecimal deliveryFee;
    private BigDecimal platformFee;

    private BigDecimal platformDiscount;
    private BigDecimal storeDiscount;
    private BigDecimal totalDiscount;

    private BigDecimal totalAmount;

    private VoucherPricingResponse appliedPlatformVoucher;
    private VoucherPricingResponse appliedStoreVoucher;

    private Boolean canCheckout;
    private List<CartValidationResponse.ValidationIssueResponse> issues;
}
