package com.db.foodara.dto.response.store;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupResponse {
    private String id;
    private String storeId;
    private String name;
    private Boolean isRequired;
    private Integer minSelections;
    private Integer maxSelections;
    private Integer displayOrder;
    private List<OptionItemResponse> options;
}
