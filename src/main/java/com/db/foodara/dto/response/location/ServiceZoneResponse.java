package com.db.foodara.dto.response.location;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceZoneResponse {
    private UUID id;
    private String name;
    private UUID cityId;
    private boolean isActive;
    private BigDecimal surgeMultiplier;
}