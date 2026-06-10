package com.db.foodara.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceOrderResponse {

    private String orderId;
    private String orderNumber;
    private String status;
    private String paymentMethod;
    private String paymentStatus;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal platformFee;
    private BigDecimal voucherDiscount;
    private BigDecimal totalAmount;

    // SePay checkout URL (null if COD) — kept for backward compatibility
    private String checkoutUrl;

    // Timestamps
    private LocalDateTime placedAt;

    // Delivery info
    private Integer estimatedDeliveryTime;
}
