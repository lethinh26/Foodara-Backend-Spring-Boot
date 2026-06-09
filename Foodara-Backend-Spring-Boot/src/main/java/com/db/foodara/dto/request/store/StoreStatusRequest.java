package com.db.foodara.dto.request.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreStatusRequest {
    private boolean open;
    private boolean active;
    private boolean autoAcceptOrders;

}
