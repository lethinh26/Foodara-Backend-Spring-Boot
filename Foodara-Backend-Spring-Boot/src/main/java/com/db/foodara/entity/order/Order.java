package com.db.foodara.entity.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "driver_id")
    private String driverId;

    // Status
    @Column(name = "status", nullable = false)
    private String status; // pending -> confirmed -> ready_for_pickup -> picked_up -> delivered

    // Delivery Address
    @Column(name = "delivery_address_id")
    private String deliveryAddressId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "delivery_address_snapshot", columnDefinition = "jsonb")
    private String deliveryAddressSnapshot;

    @Column(name = "delivery_latitude")
    private BigDecimal deliveryLatitude;

    @Column(name = "delivery_longitude")
    private BigDecimal deliveryLongitude;

    @Column(name = "delivery_note")
    private String deliveryNote;

    // Store Snapshot
    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_address")
    private String storeAddress;

    @Column(name = "store_latitude")
    private BigDecimal storeLatitude;

    @Column(name = "store_longitude")
    private BigDecimal storeLongitude;

    @Transient
    private String storeLogoUrl;

    @Transient
    private String storePhone;

    /**
     * Populated on demand by services when returning the order to the customer
     * (e.g. {@code GET /v1/orders/{id}}). Not persisted.
     */
    @Transient
    private List<OrderStatusHistory> statusHistory;

    /** Populated on demand — order line items for detail views. */
    @Transient
    private List<OrderItem> orderItems;

    // Pricing
    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
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

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    // Vouchers
    @Column(name = "platform_voucher_id")
    private String platformVoucherId;

    @Column(name = "store_voucher_id")
    private String storeVoucherId;

    // Payment
    @Column(name = "payment_method")
    private String paymentMethod; // cod, e_wallet, card, bank_transfer, qr

    @Column(name = "payment_status")
    private String paymentStatus; // pending, paid, failed

    @Column(name = "refund_status")
    private String refundStatus; // bank, voucher — null if not refunded

    // Time estimates
    @Column(name = "estimated_prep_time")
    private Integer estimatedPrepTime;

    @Column(name = "estimated_delivery_time")
    private Integer estimatedDeliveryTime;

    @Column(name = "estimated_total_time")
    private Integer estimatedTotalTime;

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

    // Delivery metrics
    @Column(name = "delivery_distance_km")
    private BigDecimal deliveryDistanceKm;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate;

    @Column(name = "commission_amount")
    private BigDecimal commissionAmount;

    // Verify
    @Column(name = "pickup_code")
    private String pickupCode;

    @Column(name = "delivery_otp")
    private String deliveryOtp;

    @Column(name = "delivery_proof_url")
    private String deliveryProofUrl;

    // Store response
    @Column(name = "store_response_deadline")
    private LocalDateTime storeResponseDeadline;

    @Column(name = "store_responded_at")
    private LocalDateTime storeRespondedAt;

    // Reorder
    @Column(name = "is_reorder")
    private Boolean isReorder = false;

    @Column(name = "original_order_id")
    private String originalOrderId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "pending";
        if (paymentStatus == null) paymentStatus = "pending";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}