package com.db.foodara.dto.response.store;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboResponse {
    private String id;
    private String storeId;
    private String name;
    private String description;
    private BigDecimal comboPrice;
    private BigDecimal originalPrice;
    private Boolean isActive;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private List<ComboItemResponse> items;

    @Getter
    @Setter
    @Builder
    public static class ComboItemResponse {
        private String id;
        private String menuItemId;
        private String menuItemName;
        private Integer quantity;
    }
}
