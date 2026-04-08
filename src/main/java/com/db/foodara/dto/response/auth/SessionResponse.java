package com.db.foodara.dto.response.auth;

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
    private String browser;
    private String os;
    private String ipAddress;
    private String country;
    private String city;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;
    private boolean current;
}
