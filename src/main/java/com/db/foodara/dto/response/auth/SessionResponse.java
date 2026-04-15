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
    private String ipAddress;
    private LocalDateTime createdAt;
    private boolean current;
}
