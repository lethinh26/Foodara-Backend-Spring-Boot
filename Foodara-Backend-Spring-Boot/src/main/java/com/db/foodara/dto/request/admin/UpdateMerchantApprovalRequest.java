package com.db.foodara.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMerchantApprovalRequest {
    @NotBlank(message = "Approval status is required")
    private String approvalStatus; // pending, approved, rejected, suspended
}
