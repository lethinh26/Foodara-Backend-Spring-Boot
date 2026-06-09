package com.db.foodara.dto.response.admin;

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
public class AdminReviewResponse {
    private String id;
    private String orderId;
    private String orderNumber;
    private String userId;
    private String userName;
    private String storeId;
    private String storeName;
    private Short storeRating;
    private String storeComment;
    private String driverId;
    private String driverName;
    private Short driverRating;
    private String driverComment;
    private Boolean isAnonymous;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Loaded for detail view
    private List<ReviewImageDto> images;
    private List<ReviewItemDto> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewImageDto {
        private String id;
        private String reviewId;
        private String imageUrl;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewItemDto {
        private String id;
        private String reviewId;
        private String menuItemId;
        private String menuItemName;
        private Short rating;
        private String comment;
    }
}
