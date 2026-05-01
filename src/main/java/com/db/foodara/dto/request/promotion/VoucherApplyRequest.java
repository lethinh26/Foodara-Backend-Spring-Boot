package com.db.foodara.dto.request.promotion;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoucherApplyRequest {

    @NotBlank(message = "Store ID is required")
    private String storeId;

    // Platform discount voucher (percentage/fixed type)
    private String platformVoucherId;
    private String platformCode;

    // Platform freeship voucher
    private String platformShipVoucherId;
    private String platformShipCode;

    // Store voucher
    private String storeVoucherId;
    private String storeCode;
}
