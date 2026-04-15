package com.db.foodara.entity.merchant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "store_documents")
@Getter
@Setter
public class StoreDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "store_id")
    private String storeId;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_url", nullable = false, length = 500)
    private String documentUrl;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "verification_status", length = 20)
    private String verificationStatus = "pending";

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (verificationStatus == null) verificationStatus = "pending";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
