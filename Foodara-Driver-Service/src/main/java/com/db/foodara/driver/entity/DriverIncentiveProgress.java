package com.db.foodara.driver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_incentive_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"program_id", "driver_id"})
})
@Getter
@Setter
public class DriverIncentiveProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "program_id", nullable = false)
    private String programId;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "current_value")
    private Integer currentValue = 0;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "bonus_paid")
    private Boolean bonusPaid = false;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

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
