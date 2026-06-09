package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminStoreDocumentResponse {
    private String id;
    private String merchantId;
    private String storeId;
    private String documentType;
    private String documentUrl;
    private String documentNumber;
    private LocalDate expiryDate;
    private String verificationStatus;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
