package com.db.foodara.dto.internal.mapbox;

import java.math.BigDecimal;

public record DirectionsResult(
        BigDecimal distanceKm,
        Integer durationMinutes,
        String polyline
) {}
