package com.db.foodara.dto.response.order;

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
public class CartValidationResponse {

    private Boolean valid;
    private BigDecimal subtotal;
    private BigDecimal minOrderAmount;
    private BigDecimal shortfallAmount;
    private List<ValidationIssueResponse> issues;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationIssueResponse {
        private String code;
        private String message;
        private String cartItemId;
    }
}
