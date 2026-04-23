package com.db.foodara.entity.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotNull
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "driver_id")
    private Long driverId;

    // Pricing
    @NotNull
    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee;

    @Column(name = "delivery_fee_discount")
    private BigDecimal deliveryFeeDiscount;

    @Column(name = "platform_fee")
    private BigDecimal platformFee;

    @Column(name = "surge_fee")
    private BigDecimal surgeFee;

    @Column(name = "store_discount")
    private BigDecimal storeDiscount;

    @Column(name = "voucher_discount")
    private BigDecimal voucherDiscount;

    @NotNull
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "platform_voucher_id")
    private String platformVoucherId;

    @Column(name = "store_voucher_id")
    private String storeVoucherId;

    // Delivery & Snapshots
    @Column(name = "delivery_address_id")
    private Long deliveryAddressId;

    @Column(name = "delivery_address_snapshot", columnDefinition = "jsonb")
    private String deliveryAddressSnapshot;

    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude")
    private Double deliveryLongitude;

    @Column(name = "delivery_note")
    private String deliveryNote;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_address")
    private String storeAddress;

    @Column(name = "store_latitude")
    private Double storeLatitude;

    @Column(name = "store_longitude")
    private Double storeLongitude;

    // Payment & Status
    @NotBlank
    @Column(name = "payment_method")
    private String paymentMethod; // cod, e_wallet, card, bank_transfer, qr

    @NotBlank
    @Column(name = "payment_status")
    private String paymentStatus; // pending, paid, failed, refunded...

    @NotBlank
    @Column(name = "status")
    private String status; // pending -> confirmed -> ... -> completed

    // Timestamps
    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "preparing_at")
    private LocalDateTime preparingAt;

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Cancel
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private String cancelledBy; // customer, store, driver, admin, system

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    // Verify
    @Column(name = "pickup_code")
    private String pickupCode;

    @Column(name = "delivery_otp")
    private String deliveryOtp;

    @Column(name = "delivery_proof_url")
    private String deliveryProofUrl;

    // Reorder
    @Column(name = "is_reorder")
    private Boolean isReorder = false;

    @Column(name = "original_order_id")
    private Long originalOrderId;
}