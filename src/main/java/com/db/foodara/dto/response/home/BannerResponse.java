package com.db.foodara.dto.response.home;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponse {

    private String id;
    private String title;
    private String imageUrl;
    private String targetUrl;
    private String targetType;
    private String targetId;
    private String position;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private LocalDateTime createdAt;
}