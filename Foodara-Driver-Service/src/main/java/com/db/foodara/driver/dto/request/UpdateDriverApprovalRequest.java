package com.db.foodara.driver.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDriverApprovalRequest {
    @NotBlank(message = "Approval status is required")
    private String approvalStatus;

    private String reason;
}
