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
public class StoreSettlementItemResponse {
    private String id;
    private String settlementId;
    private String storeId;
    private String storeName;
    private String orderId;
    private String orderNumber;
    private BigDecimal orderSubtotal;
    private BigDecimal commissionAmount;
    private BigDecimal voucherSubsidy;
    private BigDecimal deduction;
    private BigDecimal netAmount;
    private LocalDateTime createdAt;
}
