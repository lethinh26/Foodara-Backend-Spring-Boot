package com.db.foodara.entity.home;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "campaign_participants")
public class CampaignParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "campaign_id", nullable = false)
    private String campaignId;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    private String status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @PrePersist
    protected void OnCreate(){
        status = "active";
        joinedAt = LocalDateTime.now();
    }
}
