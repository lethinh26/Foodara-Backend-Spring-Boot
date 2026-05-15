package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyStatsResponse {
    private LocalDate statDate;
    private int totalOrders;
    private int totalCompletedOrders;
    private int totalCancelledOrders;
    private BigDecimal totalGmv;
    private BigDecimal totalRevenue;
    private BigDecimal avgOrderValue;
    private int avgDeliveryTimeMinutes;
    private double cancellationRate;
    private int newUsers;
    private int newStores;
    private int newDrivers;
    private int activeUsers;
    private int activeStores;
    private int activeDrivers;
}
