package com.db.foodara.dto.response.store;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StoreCategoryResponse {
    private String id;
    private String name;
    private String slug;
    private String iconUrl;
    private int displayOrder;
    private boolean isActive;
    private LocalDateTime createdAt;
}
