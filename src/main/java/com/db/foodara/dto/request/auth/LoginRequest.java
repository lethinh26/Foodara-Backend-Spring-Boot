package com.db.foodara.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Email or phone is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
