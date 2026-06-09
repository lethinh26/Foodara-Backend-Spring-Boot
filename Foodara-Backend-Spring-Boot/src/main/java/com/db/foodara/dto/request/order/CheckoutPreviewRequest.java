package com.db.foodara.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutPreviewRequest {

    @NotBlank(message = "Store ID is required")
    private String storeId;

    private String addressId;

    private String platformCode;
    private String storeCode;

    private String platformVoucherId;
    private String storeVoucherId;
}
