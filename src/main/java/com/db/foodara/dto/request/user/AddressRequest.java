package com.db.foodara.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddressRequest {
    @NotBlank(message = "Label is required")
    private String label;

    private String recipientName;
    private String recipientPhone;

    @NotBlank(message = "Full address is required")
    private String addressLine;

    private String ward;
    private String districtId;
    private String cityId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String deliveryNote;
    private boolean isDefault;
}
