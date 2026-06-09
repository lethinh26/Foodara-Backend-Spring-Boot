package com.db.foodara.dto.response.location;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DirectionsResponse(
        BigDecimal distanceKm,
        Integer durationMinutes,
        String polyline
) {}
