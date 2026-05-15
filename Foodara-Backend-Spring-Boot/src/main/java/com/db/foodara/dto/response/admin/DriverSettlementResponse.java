package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverSettlementResponse {
    private String id;
    private String driverId;
    private String driverName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalDeliveries;
    private BigDecimal totalDeliveryEarnings;
    private BigDecimal totalTips;
    private BigDecimal totalBonuses;
    private BigDecimal totalCodCollected;
    private BigDecimal totalCodTransferred;
    private BigDecimal totalDeductions;
    private BigDecimal netAmount;
    private String status;
    private LocalDateTime paidAt;
    private String paymentReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
