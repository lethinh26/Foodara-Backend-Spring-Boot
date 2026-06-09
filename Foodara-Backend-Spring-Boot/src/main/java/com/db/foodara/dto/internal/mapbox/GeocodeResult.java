package com.db.foodara.dto.internal.mapbox;

import java.math.BigDecimal;

public record GeocodeResult(
        BigDecimal latitude,
        BigDecimal longitude,
        String formattedAddress,
        String ward,
        String district,
        String city
) {}
