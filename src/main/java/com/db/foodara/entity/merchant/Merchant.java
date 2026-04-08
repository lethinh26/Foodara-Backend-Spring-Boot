package com.db.foodara.entity.merchant;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "merchants")
public class Merchant {
    @Id
    @GeneratedValue(generator = "UUID")
    private String id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column( length = 255)
    @Size(min = 6,  message = "MERCHANT_NAME_INVALID")
    private String name;


    @Size(max = 13, min = 10,message = "TAX_CODE_INVALID")
    private String taxCode;

    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$",
            message = "MERCHANT_EMAIL_INVALID"
    )
    @Column(name = "business_email" ,nullable = false, length = 255)
    @Size(min = 8)
    private String businessEmail;

    @Column(name = "business_phone", length = 20)
    @Pattern(
            regexp = "0[0-9]{9}",
            message = "MERCHANT_PHONE_INVALID"
    )
    private String businessPhone;

    @Column(name = "logo_url")
    private String logoUrl;
    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "approval_status")
    private ApprovalMerchantStatus approvalStatus = ApprovalMerchantStatus.PENDING;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "update_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

}
