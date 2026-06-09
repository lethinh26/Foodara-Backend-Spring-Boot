package com.db.foodara.dto.request.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountRequest {
    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @NotBlank(message = "Account number is required")
    @Size(max = 30, message = "Account number must not exceed 30 characters")
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(max = 255, message = "Account holder name must not exceed 255 characters")
    private String accountHolder;

    @Size(max = 255, message = "Branch must not exceed 255 characters")
    private String branch;

    private Boolean isDefault;
}
