package com.db.foodara.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeliveryFeeBatchItemResponse(
        String storeId,
        BigDecimal distanceKm,
        Integer etaMinutes,
        BigDecimal deliveryFee,
        BigDecimal surgeMultiplier,
        String error
) {}
