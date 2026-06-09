package com.db.foodara.dto.request.merchant.document;

import com.db.foodara.entity.merchant.document.DocumentType;
import com.db.foodara.entity.merchant.document.VerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRequest {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;
}
