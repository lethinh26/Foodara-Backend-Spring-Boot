package com.db.foodara.dto.request.merchant;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MerchantUpdateProfileRequest {
//    private String name;
//    private String businessPhone;
//    private String businessEmail;
//    private String taxCode;
//    private String logoUrl;
//    private String coverImageUrl;

    @Size(min = 6, message = "MERCHANT_NAME_INVALID")
    private String name;

    @Pattern(
            regexp = "0[0-9]{9}",
            message = "MERCHANT_PHONE_INVALID"
    )
    private String businessPhone;

    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$",
            message = "MERCHANT_EMAIL_INVALID"
    )
    @Size(min = 8, message = "MERCHANT_EMAIL_INVALID")
    private String businessEmail;

    @Size(max = 13, min = 10, message = "TAX_CODE_INVALID")
    private String taxCode;

    private String logoUrl;

    private String coverImageUrl;


}
