package com.db.foodara.dto.request.merchant;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StoreUpdateRequest {
    @Size(min = 2, max = 255, message = "Store name must be between 2 and 255 characters")
    private String name;

    private String slug;
    private String description;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private String addressLine;
    private String ward;
    private String districtId;
    private String cityId;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String serviceZoneId;

    private Boolean autoAcceptOrders;
    private Integer avgPreparationTime;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDeliveryRadiusKm;

    private String coverImageUrl;
    private String logoUrl;
}
