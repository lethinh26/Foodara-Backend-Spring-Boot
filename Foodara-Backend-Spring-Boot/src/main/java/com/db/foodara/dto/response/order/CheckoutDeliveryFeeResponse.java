package com.db.foodara.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckoutDeliveryFeeResponse {

    private String storeId;
    private String addressId;
    private BigDecimal distanceKm;
    private BigDecimal deliveryFee;
}
