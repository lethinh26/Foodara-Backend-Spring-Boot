package com.db.foodara.entity.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class User {
    // lớp lưu tru user
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // sài dữ liệu dạng uuid

    @Column(nullable = false, length = 255) // @column -> đại diện cho 1 cot trong bảng
    private String email; // ko cho phép null và độ dài kt 255
    // unique -> đặt quyền không được trùng


    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 255)
    private String fullName;

    @Column(length = 20)
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
