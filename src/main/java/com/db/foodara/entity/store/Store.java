package com.db.foodara.entity.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stores")
@Getter
@Setter
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug")
    private String slug;

    @Column(name = "description")
    private String description;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "ward")
    private String ward;

    @Column(name = "district_id")
    private String districtId;

    @Column(name = "city_id")
    private String cityId;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "service_zone_id")
    private String serviceZoneId;

    @Column(name = "is_open")
    private Boolean isOpen;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "auto_accept_orders")
    private Boolean autoAcceptOrders;

    @Column(name = "avg_preparation_time")
    private Integer avgPreparationTime;

    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount;

    @Column(name = "max_delivery_radius_km")
    private BigDecimal maxDeliveryRadiusKm;

    @Column(name = "avg_rating")
    private BigDecimal avgRating;

    @Column(name = "total_ratings")
    private Integer totalRatings;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isOpen == null) isOpen = false;
        if (avgRating == null) avgRating = BigDecimal.ZERO;
        if (totalRatings == null) totalRatings = 0;
        if (totalOrders == null) totalOrders = 0;
        if (commissionRate == null) commissionRate = BigDecimal.valueOf(20);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}