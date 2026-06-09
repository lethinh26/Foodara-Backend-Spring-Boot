package com.db.foodara.dto.response.location;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeocodeResponse {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String formattedAddress;
    private String addressLine;
    private String ward;
    private String districtName;
    private String cityName;
}
