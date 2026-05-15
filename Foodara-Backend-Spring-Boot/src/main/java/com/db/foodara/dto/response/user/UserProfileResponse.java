package com.db.foodara.dto.response.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String status;
    private boolean emailVerified;
    private List<String> roles;
    private LocalDateTime createdAt;
}