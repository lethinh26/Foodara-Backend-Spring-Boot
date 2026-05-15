package com.db.foodara.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String passwordHash;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String status;
}
