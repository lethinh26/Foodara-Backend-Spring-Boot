package com.db.foodara.dto.response.store;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {

    private String id;
    private String orderId;
    private String userId;
    private String storeId;
    private Short storeRating;
    private String storeComment;
    private String driverId;
    private Short driverRating;
    private String driverComment;
    private Boolean isAnonymous;
    private String status;
    private LocalDateTime createdAt;

    // Display fields (populated by join or lookup)
    private String customerName;
    private String customerAvatar;
}
