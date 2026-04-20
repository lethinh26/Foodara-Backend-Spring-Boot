package com.db.foodara.dto.response.location;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageCheckResponse {
    private boolean covered;
    private String cityId;
    private String cityName;
    private String zoneId;
    private String zoneName;
    private BigDecimal surgeMultiplier;
}