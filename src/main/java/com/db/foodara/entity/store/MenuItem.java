package com.db.foodara.entity.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "track_inventory")
    private Boolean trackInventory;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "max_quantity_per_order")
    private Integer maxQuantityPerOrder;

    @Column(name = "daily_limit")
    private Integer dailyLimit;

    @Column(name = "daily_sold_count")
    private Integer dailySoldCount;

    @Column(name = "daily_sold_date")
    private java.time.LocalDate dailySoldDate;

    @Column(name = "is_popular")
    private Boolean isPopular;

    @Column(name = "is_new")
    private Boolean isNew;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "avg_rating")
    private BigDecimal avgRating;

    @Column(name = "total_ratings")
    private Integer totalRatings;

    @Column(name = "total_sold")
    private Integer totalSold;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isAvailable == null) isAvailable = true;
        if (isActive == null) isActive = true;
        if (trackInventory == null) trackInventory = false;
        if (isPopular == null) isPopular = false;
        if (isNew == null) isNew = false;
        if (avgRating == null) avgRating = BigDecimal.ZERO;
        if (totalRatings == null) totalRatings = 0;
        if (totalSold == null) totalSold = 0;
        if (dailySoldCount == null) dailySoldCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}