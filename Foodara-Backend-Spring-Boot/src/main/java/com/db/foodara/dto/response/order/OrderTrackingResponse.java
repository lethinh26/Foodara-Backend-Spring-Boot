package com.db.foodara.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderTrackingResponse {

    private String orderId;
    private String orderNumber;
    private String status;

    // Store
    private String storeId;
    private String storeName;
    private BigDecimal storeLatitude;
    private BigDecimal storeLongitude;

    // Delivery location
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;

    // Driver (null if not assigned yet)
    private String driverId;
    private String driverName;
    private String driverPhone;
    private BigDecimal driverLatitude;
    private BigDecimal driverLongitude;

    // Route
    private BigDecimal distanceKm;
    private Integer etaMinutes;
    private String polyline;
}
