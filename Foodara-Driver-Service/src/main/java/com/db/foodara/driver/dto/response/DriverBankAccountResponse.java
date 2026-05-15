package com.db.foodara.driver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverBankAccountResponse {
    private String id;
    private String driverId;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String branch;
    private Boolean isDefault;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
