package com.db.foodara.entity.home;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Getter
@Setter
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "target_url")
    private String targetUrl;

    @Column(name = "target_type")
    private String targetType;

    private String targetId;

    @Column(name = "position")
    private String position;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (displayOrder == null) displayOrder = 0;
        if (isActive == null) isActive = true;
    }
}
