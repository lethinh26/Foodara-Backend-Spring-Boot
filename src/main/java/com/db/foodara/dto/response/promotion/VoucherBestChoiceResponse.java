package com.db.foodara.dto.response.promotion;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoucherBestChoiceResponse {

    private VoucherPricingResponse platformVoucher;
    private VoucherPricingResponse storeVoucher;
    private BigDecimal totalDiscount;
}
