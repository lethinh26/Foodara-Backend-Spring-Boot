package com.db.foodara.dto.response.promotion;

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
public class VoucherCartPricingResponse {

    private String storeId;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal subtotalAfterVoucher;

    private VoucherPricingResponse appliedPlatformVoucher;
    private VoucherPricingResponse appliedStoreVoucher;

    private VoucherPricingResponse bestPlatformVoucher;
    private VoucherPricingResponse bestStoreVoucher;

    private List<VoucherResponse> availableVouchers;

    private boolean canApply;
    private String message;
}
