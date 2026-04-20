package com.db.foodara.entity.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "store_rating")
    private Short storeRating;

    @Column(name = "store_comment")
    private String storeComment;

    @Column(name = "driver_id")
    private String driverId;

    @Column(name = "driver_rating")
    private Short driverRating;

    @Column(name = "driver_comment")
    private String driverComment;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isAnonymous == null) isAnonymous = false;
        if (status == null) status = "active";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}