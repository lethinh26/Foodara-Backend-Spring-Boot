package com.db.foodara.driver.dto.response;

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
public class DriverShiftResponse {
    private String id;
    private String driverId;
    private LocalDateTime wentOnlineAt;
    private LocalDateTime wentOfflineAt;
    private Integer durationMinutes;
    private Integer totalOrders;
    private BigDecimal totalEarnings;
    private LocalDateTime createdAt;
}
