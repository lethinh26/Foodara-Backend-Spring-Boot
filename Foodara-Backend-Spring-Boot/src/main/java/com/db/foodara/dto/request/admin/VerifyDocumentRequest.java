package com.db.foodara.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyDocumentRequest {
    @NotBlank(message = "Verification status is required")
    private String verificationStatus; // verified, rejected
}
