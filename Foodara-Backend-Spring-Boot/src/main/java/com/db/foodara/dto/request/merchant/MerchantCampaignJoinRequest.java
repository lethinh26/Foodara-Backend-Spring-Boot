package com.db.foodara.dto.request.merchant;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantCampaignJoinRequest {

    @NotBlank(message = "Campaign ID is required")
    private String campaignId;

    @NotBlank(message = "Store ID is required")
    private String storeId;
}
