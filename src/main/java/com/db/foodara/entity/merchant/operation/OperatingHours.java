package com.db.foodara.entity.merchant.operation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "store_operating_hours",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"store_id", "day", "open_time"})
    }
)
public class OperatingHours {
    @Id
    private String id;

    @Column(name = "store_id", nullable = false)
    String storeId;

    int day;

    @Column(name = "day_of_week", nullable = false)
    int day_of_week = 0;

    @Column(name="open_time", nullable = false)
    int openTime = 0;

    @Column(name = "close_time", nullable = false)
    int closeTime = 0;

    @Column(name = "is_close", nullable = false)
    boolean isClose = true;

}
