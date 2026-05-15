package com.db.foodara.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleWithPermissionsResponse {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<PermissionResponse> permissions;
}
