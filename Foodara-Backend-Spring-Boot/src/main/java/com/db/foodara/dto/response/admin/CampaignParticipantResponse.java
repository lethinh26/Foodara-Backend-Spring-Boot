package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CampaignParticipantResponse {
    private String id;
    private String campaignId;
    private String storeId;
    private String storeName;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime endedAt;
}
