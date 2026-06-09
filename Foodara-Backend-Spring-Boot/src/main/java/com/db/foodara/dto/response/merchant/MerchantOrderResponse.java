package com.db.foodara.dto.response.merchant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Merchant-facing order projection.
 * Includes the data needed by the merchant's Inbox / Kitchen / Handover screens
 * (customer contact, line items, pricing, timestamps) without exposing the full
 * customer-side payload.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantOrderResponse {

    private String id;
    private String orderNumber;
    private String storeId;

    private String customerId;
    private String customerName;
    private String customerPhone;

    private String driverId;

    /** Lowercase status: pending | confirmed | preparing | ready_for_pickup | picked_up | delivered | completed | cancelled. */
    private String status;
    private String paymentMethod;
    private String paymentStatus;

    // Pricing snapshot
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal storeDiscount;
    private BigDecimal voucherDiscount;
    private BigDecimal totalAmount;

    // Order codes
    private String pickupCode;

    // Notes
    private String deliveryNote;
    private String cancellationReason;

    // Items
    private List<MerchantOrderItemResponse> items;

    // Timestamps
    private LocalDateTime placedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
