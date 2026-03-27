package com.db.foodara.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    @NotBlank(message = "INVALID_KEY") // nếu để trống (Not blank) -> gọi tới invalidkey
    @Email(message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "USERNAME_INVALID")
    @Size(min = 3, message = "USERNAME_INVALID")
    private String fullName;

    @NotBlank(message = "INVALID_KEY")
    @Pattern(regexp = "0[0-9]{9}", message = "PHONE_INVALID") // thêm ErrorCode mới
    private String phone;

    @NotBlank(message = "PASSWORD_INVALID")
    @Size(min = 8, message = "PASSWORD_INVALID")
    private String passwordHash;

    private String avatarUrl;
    private String status;

}
