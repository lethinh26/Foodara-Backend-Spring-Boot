package com.db.foodara.driver.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyDriverDocumentRequest {
    @NotBlank(message = "Verification status is required")
    private String verificationStatus;

    private String rejectionReason;
}
