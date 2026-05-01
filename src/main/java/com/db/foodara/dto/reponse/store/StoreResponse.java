package com.db.foodara.dto.reponse.store;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class StoreResponse {
    private String id;
    private String merchantId;
    private String name;
    private String slug;
    private String addressLine;
    private String ward;
    private String districtId;
    private String city_id;
    private String latitude;
    private String longitude;
    private String serviceZone;
}
