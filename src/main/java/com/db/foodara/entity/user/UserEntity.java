package com.db.foodara.entity.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class UserEntity {
    // lớp lưu tru user
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // sài dữ liệu dạng uuid

    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$",
            message = "EMAIL_INVALID"
    )
    @Column(nullable = false, length = 255) // @column -> đại diện cho 1 cot trong bảng
    private String email;

    @Column(nullable = false, length = 255)
    @Size(min = 6, message = "PASSWORD_INVALID")
    private String passwordHash;

    @Column(length = 255)
    @Size(min = 8, message = "USERNAME_INVALID")
    private String fullName;

    @Column(length = 20)
    @Pattern(
            regexp = "0[0-9]{9}",
            message = "PHONE_INVALID"
    )
    private String phone;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 20)
    private String status;

    @Column
    private LocalDateTime emailVerifiedAt;

    @Column
    private LocalDateTime lastLoginAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
