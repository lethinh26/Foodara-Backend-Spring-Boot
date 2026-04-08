package com.db.foodara.dto.response.location;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityResponse {
    private String id;
    private String name;
    private String code;
    private boolean isActive;
}
