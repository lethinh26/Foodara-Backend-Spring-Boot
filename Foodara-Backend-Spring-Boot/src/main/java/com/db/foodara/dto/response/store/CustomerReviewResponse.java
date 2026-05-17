package com.db.foodara.dto.response.store;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerReviewResponse {
    private String id;
    private String orderId;
    private String storeId;
    private Integer storeRating;
    private String storeComment;
    private Integer driverRating;
    private String driverComment;
    private Boolean isAnonymous;
    private String status;
    private List<String> tags;
    private List<ItemResponse> items;
    private LocalDateTime createdAt;

    // For store reviews listing (public view)
    private String customerName;
    private String customerAvatar;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        private String menuItemId;
        private String menuItemName;
        private Integer rating;
        private String comment;
    }
}
