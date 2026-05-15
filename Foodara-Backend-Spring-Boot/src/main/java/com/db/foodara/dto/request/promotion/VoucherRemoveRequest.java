package com.db.foodara.dto.request.promotion;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoucherRemoveRequest {

    @NotBlank(message = "Store ID is required")
    private String storeId;

    // API contract: "platform" | "store" | "all"(optional)
    private String type;

    // Current selected vouchers (optional), prioritized by code
    private String platformCode;
    private String storeCode;

    // Backward compatibility for existing clients
    private String platformVoucherId;
    private String storeVoucherId;

    @Deprecated
    private boolean removePlatform;

    @Deprecated
    private boolean removeStore;
}
