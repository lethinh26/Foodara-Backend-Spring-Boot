package com.db.foodara.dto.request.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MerchantDocumentRequest {
    private String storeId;

    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotBlank(message = "Document URL is required")
    @Size(max = 500, message = "Document URL must not exceed 500 characters")
    private String documentUrl;

    @Size(max = 100, message = "Document number must not exceed 100 characters")
    private String documentNumber;

    private LocalDate expiryDate;
}
