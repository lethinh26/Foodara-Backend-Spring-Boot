package com.db.foodara.dto.response.merchant;

import java.math.BigDecimal;

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
public class MerchantRevenuePoint {
    /** YYYY-MM-DD */
    private String date;
    /** Short label (e.g. "T2", "T3"... in Vietnamese, or date) */
    private String day;
    private BigDecimal revenue;
    private Long orders;
}
