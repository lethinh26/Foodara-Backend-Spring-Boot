package com.db.foodara.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {
    @NotBlank(message = "INVALID_KEY")
    private String phone;

    @NotBlank(message = "PASSWORD_INVALID")
    private String password;
}
