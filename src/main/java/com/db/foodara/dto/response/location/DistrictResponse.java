package com.db.foodara.dto.response.location;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistrictResponse {
    private String id;
    private String cityId;
    private String name;
    private String code;
    private boolean isActive;
}