package com.db.foodara.dto.reponse.auth;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String id;
    private String deviceName;
    private String ipAddress;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;
    private boolean current;
}
