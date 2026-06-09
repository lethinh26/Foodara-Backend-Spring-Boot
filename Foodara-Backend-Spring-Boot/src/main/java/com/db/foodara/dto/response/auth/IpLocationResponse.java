package com.db.foodara.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IpLocationResponse {
    private String status;
    
    private String country;
    
    @JsonProperty("countryCode")
    private String countryCode;
    
    private String region;
    
    @JsonProperty("regionName")
    private String regionName;
    
    private String city;
    
    private String zip;
    
    private Double lat;
    
    private Double lon;
    
    private String timezone;
    
    private String isp;
    
    private String org;
    
    private String as;
    
    private String query;
    
    public boolean isSuccess() {
        return "success".equals(status);
    }
}
