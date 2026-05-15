package com.db.foodara.dto.request.merchant.bankaccount;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountCreateRequest {
    @NotBlank(message = "BANK_NAME_REQUIRED")
    private String bankName;
    @NotBlank(message = "ACCOUNT_NUMBER_REQUIRED")
    private String accountNumber;
//    private String branch;
}
