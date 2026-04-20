package com.db.foodara.dto.response.home;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

    private String id;
    private String name;
    private String description;
    private String campaignType;
    private String bannerUrl;
    private Boolean isActive;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private LocalDateTime createdAt;
}