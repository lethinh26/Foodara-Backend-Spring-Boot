package com.db.foodara.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceOrderRequest {

    @NotBlank(message = "Store ID is required")
    private String storeId;

    @NotBlank(message = "Address ID is required")
    private String addressId;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(cod|qr)$", message = "Payment method must be 'cod' or 'qr'")
    private String paymentMethod;

    private String note;

    private String platformVoucherId;
    private String storeVoucherId;
    private String platformCode;
    private String storeCode;
}
