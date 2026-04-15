package com.db.foodara.entity.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "option_groups")
@Getter
@Setter
public class OptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_required")
    private Boolean isRequired = false;

    @Column(name = "min_selections")
    private Integer minSelections = 0;

    @Column(name = "max_selections")
    private Integer maxSelections = 1;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
