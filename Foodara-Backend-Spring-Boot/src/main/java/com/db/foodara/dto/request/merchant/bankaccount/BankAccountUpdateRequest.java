package com.db.foodara.dto.request.merchant.bankaccount;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountUpdateRequest {
    private String bankName;
    private String branch; // storeid
}
