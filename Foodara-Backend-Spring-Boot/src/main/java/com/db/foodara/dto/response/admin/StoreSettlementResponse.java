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
public class StoreSettlementResponse {
    private String id;
    private String merchantId;
    private String merchantName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalOrders;
    private BigDecimal totalGmv;
    private BigDecimal totalCommission;
    private BigDecimal totalVoucherSubsidy;
    private BigDecimal totalDeductions;
    private BigDecimal netAmount;
    private String status;
    private LocalDateTime paidAt;
    private String paymentReference;
    private String createdBy;
    private String confirmedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
