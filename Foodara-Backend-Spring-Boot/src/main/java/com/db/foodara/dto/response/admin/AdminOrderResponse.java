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
public class AdminOrderResponse {
    private String id;
    private String orderNumber;

    // Customer
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Store
    private String storeId;
    private String storeName;
    private String storeAddress;

    // Driver
    private String driverId;
    private String driverName;
    private String driverPhone;

    // Status
    private String status;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal deliveryFeeDiscount;
    private BigDecimal platformFee;
    private BigDecimal surgeFee;
    private BigDecimal storeDiscount;
    private BigDecimal voucherDiscount;
    private BigDecimal totalAmount;

    // Payment
    private String paymentMethod;
    private String paymentStatus;

    // Delivery
    private String deliveryNote;
    private BigDecimal deliveryDistanceKm;

    // Time estimates
    private Integer estimatedPrepTime;
    private Integer estimatedDeliveryTime;
    private Integer estimatedTotalTime;

    // Timestamps
    private LocalDateTime placedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private String cancellationReason;

    // Commission
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
