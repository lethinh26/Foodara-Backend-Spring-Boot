package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminMerchantResponse {
    private String id;
    private String ownerId;
    private String ownerName;
    private String ownerEmail;
    private String name;
    private String taxCode;
    private String businessEmail;
    private String businessPhone;
    private String logoUrl;
    private String coverImageUrl;
    private String approvalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer storeCount;
}
