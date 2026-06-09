package com.db.foodara.entity.promotion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "campaign_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"campaign_id", "store_id"}))
@Getter
@Setter
public class CampaignParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "campaign_id", nullable = false)
    private String campaignId;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = "active";
        if (joinedAt == null) joinedAt = LocalDateTime.now();
    }
}
