package com.db.foodara.dto.request.merchant;


import com.db.foodara.exception.ErrorCode;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantRegisterRequest {
    @Size(min = 6,  message = "MERCHANT_NAME_INVALID")
    private String name;
    private String ownerId;
    @Size(min = 10, max = 13, message = "TAX_CODE_INVALID")
    private String taxCode;
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$",
            message = "MERCHANT_EMAIL_INVALID"
    )
    private String businessEmail;
    @Pattern(
            regexp = "0[0-9]{9}",
            message = "MERCHANT_PHONE_INVALID"
    )
    private String businessPhone;
    private String logoUrl;
    private String coverImageUrl;
}
