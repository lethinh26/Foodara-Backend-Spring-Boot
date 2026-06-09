package com.db.foodara.dto.request.merchant;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantProfileRequest {
    @Size(min = 2, max = 255, message = "Business name must be between 2 and 255 characters")
    private String name;

    @Size(max = 50, message = "Tax code must not exceed 50 characters")
    private String taxCode;

    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String businessEmail;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String businessPhone;

    private String logoUrl;
    private String coverImageUrl;
}
