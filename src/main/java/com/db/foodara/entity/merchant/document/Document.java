package com.db.foodara.entity.merchant.document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "documents")
public class Document {
    @Id
    private String id;

    @Column(name = "merchant_id", nullable = false)
    String merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    DocumentType documentType;
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    VerificationStatus verificationStatus = VerificationStatus.pending;


}
