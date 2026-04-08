package com.db.foodara.dto.response.store;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StoreCategoryResponse {
    private String id;
    private String name;
    private String slug;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("display_order")
    private int displayOrder;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
