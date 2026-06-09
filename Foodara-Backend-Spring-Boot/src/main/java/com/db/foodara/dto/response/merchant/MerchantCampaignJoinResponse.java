package com.db.foodara.dto.response.merchant;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantCampaignJoinResponse {
    private String id;
    private String campaignId;
    private String storeId;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime endedAt;
}
