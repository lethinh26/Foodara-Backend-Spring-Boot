package com.db.foodara.dto.reponse.user;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private String id;
    private String label;
    private String fullAddress;
    private String wardName;
    private String districtName;
    private String cityName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String note;
    private boolean isDefault;
}
