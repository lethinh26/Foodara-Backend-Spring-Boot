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
public class OrderHistoryResponse {
    private String id;
    private String fromStatus;
    private String toStatus;
    private String changedBy;
    private String changedByRole;
    private String changedByName;
    private String note;
    private LocalDateTime createdAt;
}
