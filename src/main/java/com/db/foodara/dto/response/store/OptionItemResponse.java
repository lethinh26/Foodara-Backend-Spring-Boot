package com.db.foodara.dto.response.store;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionItemResponse {
    private String id;
    private String optionGroupId;
    private String name;
    private BigDecimal priceAdjustment;
    private Boolean isAvailable;
    private Boolean isDefault;
    private Integer displayOrder;
}
