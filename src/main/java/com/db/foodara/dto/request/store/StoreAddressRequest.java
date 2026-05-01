package com.db.foodara.dto.request.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreAddressRequest {
    private String addressLine;
    private String ward;
    private String districtName;
    private String cityName;
    private String latitude;
    private String longitude;
}
