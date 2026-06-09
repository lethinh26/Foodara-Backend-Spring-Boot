package com.db.foodara.dto.request.store;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateReviewRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Store rating is required")
    @Min(1)
    @Max(5)
    private Integer storeRating;

    private String storeComment;

    @Min(1)
    @Max(5)
    private Integer driverRating;

    private String driverComment;

    private Boolean isAnonymous;

    private List<String> tags;

    private List<ReviewItemRequest> items;

    @Getter
    @Setter
    public static class ReviewItemRequest {
        private String menuItemId;
        @Min(1)
        @Max(5)
        private Integer rating;
        private String comment;
    }
}
