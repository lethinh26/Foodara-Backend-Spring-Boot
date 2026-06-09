package com.db.foodara.dto.reponse.merchant;


import com.db.foodara.entity.merchant.ApprovalMerchantStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantProfileResponse {
    private String name;
    private String taxCode;
    private String businessEmail;
    private String businessPhone;
    private String logoUrl;
    private String coverImageUrl;
    private ApprovalMerchantStatus approvalStatus;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
