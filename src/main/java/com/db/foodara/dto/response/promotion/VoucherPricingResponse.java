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
public class VoucherPricingResponse {

    private String voucherId;
    private String code;
    private String voucherType;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal potentialDiscount;
}
