package com.db.foodara.dto.request.promotion;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoucherApplyRequest {

    @NotBlank(message = "Store ID is required")
    private String storeId;

    private String platformVoucherId;

    private String storeVoucherId;
}
