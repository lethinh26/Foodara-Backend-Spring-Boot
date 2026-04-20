package com.db.foodara.dto.response.merchant;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankAccountResponse {
    private String id;
    private String merchantId;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String branch;
    private Boolean isDefault;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
