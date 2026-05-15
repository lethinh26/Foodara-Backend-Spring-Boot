package com.db.foodara.dto.request.store;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreOperationRequest {
    private int avgPreparationTime = 0;
    private int minOrderAmount = 0;
    private int maxDeliveryRadiusKm = 0;
}
