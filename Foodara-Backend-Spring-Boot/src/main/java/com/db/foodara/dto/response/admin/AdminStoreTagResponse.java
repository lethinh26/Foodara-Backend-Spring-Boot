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
public class AdminStoreTagResponse {
    private String id;
    private String name;
    private String slug;
    private String tagType;
    private String iconUrl;
    private String colorHex;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
