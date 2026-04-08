package com.db.foodara.dto.response.user;

import lombok.*;

import java.time.LocalDateTime;

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
    private boolean phoneVerified;
    private LocalDateTime createdAt;
}
