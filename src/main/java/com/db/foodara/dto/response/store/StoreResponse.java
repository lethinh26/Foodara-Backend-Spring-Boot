package com.db.foodara.dto.response.store;

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
public class StoreResponse {

    private String id;
    private String name;
    private String slug;
    private String description;
    private String phone;
    private String addressLine;
    private String ward;
    private String districtId;
    private String cityId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String serviceZoneId;
    private Boolean isOpen;
    private Boolean isActive;
    private Boolean autoAcceptOrders;
    private Integer avgPreparationTime;
    private BigDecimal minOrderAmount;
    private BigDecimal avgRating;
    private Integer totalRatings;
    private Integer totalOrders;
    private String coverImageUrl;
    private String logoUrl;
    private LocalDateTime createdAt;

    // Calculated fields for frontend
    private BigDecimal distance;
    private Integer estimatedDeliveryTime;
    private BigDecimal deliveryFee;

    // Additional info
    private Boolean hasPromotion;
    private String promotionText;
    private Boolean isNew;
    private Boolean isFeatured;
}