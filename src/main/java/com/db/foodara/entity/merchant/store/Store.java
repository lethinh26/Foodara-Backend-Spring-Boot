package com.db.foodara.entity.merchant.store;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "stores",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "slug"})
        }
)
public class Store {
    @Id
    @GeneratedValue(generator = "UUID")
    private String id;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(nullable = false, unique = true)
    @Size(min = 5, message = "STORE_NAME_INVALID")
    private String name;

    @Column(nullable = false, unique = true)
    private String slug; // url

    @Column(name = "address_line", nullable = false)
    private String addressLine; // đường

    private String ward; // phường

    @Column(name = "district_id")
    private String districtId; // quận

    @Column(name = "city_id")
    private String city_id; // thành phố

    private String latitude; // vi do
    private String longitude; // kinh do

    @Column(name = "service_zone")
    private String serviceZone;

    @Column(name = "is_open")
    private boolean isOpen = false;

    @Column(name = "is_active")
    private boolean isActive = false;

    @Column(name = "auto_accept_orders")
    private boolean autoAcceptOrders = false;

    @Column(name = "avg_preparation_time")
    private int avgPreparationTime = 0;

    @Column(name = "min_order_amount")
    private int minOrderAmount = 0;

    @Column(name = "max_delivery_radius_km")
    private int maxDeliveryRadiusKm = 0;

    @Column(name = "avg_rating")
    private double avgRating = 5;

    @Column(name = "total_ratings")
    private int totalRatings = 5;

    @Column(name = "total_orders")
    private int totalOrders = 0;

    @Column(name = "commission_rate")
    private int commissionRate = 20;

}
