package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminStoreResponse {
    private String id;
    private String merchantId;
    private String merchantName;
    private String name;
    private String slug;
    private String description;
    private String phone;
    private String addressLine;
    private String ward;
    private String districtName;
    private String cityName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isOpen;
    private Boolean isActive;
    private Boolean autoAcceptOrders;
    private Integer avgPreparationTime;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDeliveryRadiusKm;
    private BigDecimal avgRating;
    private Integer totalRatings;
    private Integer totalOrders;
    private BigDecimal commissionRate;
    private String coverImageUrl;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
