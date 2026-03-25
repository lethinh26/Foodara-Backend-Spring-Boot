package com.db.foodara.dto.request.user;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserRequest {

    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String status;

}
