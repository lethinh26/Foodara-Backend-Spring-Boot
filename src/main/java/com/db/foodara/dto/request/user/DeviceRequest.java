package com.db.foodara.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceRequest {
    @NotBlank(message = "Device token is required")
    private String deviceToken;

    private String deviceName;
    private String deviceType;
}
