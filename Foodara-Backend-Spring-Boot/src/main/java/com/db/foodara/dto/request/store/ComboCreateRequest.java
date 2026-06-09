package com.db.foodara.dto.request.store;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class ComboCreateRequest {
    private ComboRequest comboRequest;
    private List<ComboItemRequest> comboItems;
}