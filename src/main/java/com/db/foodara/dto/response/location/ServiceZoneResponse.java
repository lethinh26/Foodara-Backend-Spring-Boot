package com.db.foodara.dto.response.location;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceZoneResponse {
    private String id;
    private String name;
    private String cityId;
    private boolean isActive;
    private BigDecimal surgeMultiplier;
}
