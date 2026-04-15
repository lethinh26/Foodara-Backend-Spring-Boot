package com.db.foodara.dto.response.store;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItemResponse {

    private String id;
    private String storeId;
    private String categoryId;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal basePrice;
    private Boolean isAvailable;
    private Boolean isActive;
    private Boolean isPopular;
    private Boolean isNew;
    private Integer displayOrder;
    private BigDecimal avgRating;
    private Integer totalRatings;
    private Integer totalSold;
    private Integer maxQuantityPerOrder;
    private LocalDateTime createdAt;
}