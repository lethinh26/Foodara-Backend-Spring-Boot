package com.db.foodara.dto.reponse.user;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {
    private String id;
    private String deviceToken;
    private String deviceName;
    private String deviceType;
    private LocalDateTime createdAt;
}
