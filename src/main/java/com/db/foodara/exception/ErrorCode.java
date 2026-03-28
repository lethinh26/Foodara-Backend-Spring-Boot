package com.db.foodara.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_KEY(1001, "Invalid Message Key"),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    // Auth
    INVALID_LOGIN(1101, "Wrong credentials! Please try again"),
    UNAUTHENTICATED(1102, "Unauthenticated"),
    UNAUTHORIZED(1103, "You do not have permission"),
    INVALID_TOKEN(1104, "Invalid or expired token"),
    TOKEN_EXPIRED(1105, "Token has expired"),

    // User
    USER_EXISTED(1200, "User already exists"),
    USER_NOT_FOUND(1201, "User not found"),
    EMAIL_EXISTS(1202, "Email already exists"),
    PHONE_EXISTS(1203, "Phone already exists"),
    USERNAME_INVALID(1204, "Username must be at least 3 characters"),
    PASSWORD_INVALID(1205, "Password must be at least 8 characters"),
    EMAIL_INVALID(1206, "Email isn't valid"),
    PHONE_INVALID(1207, "Phone isn't valid"),
    WRONG_PASSWORD(1208, "Wrong password"),
    ACCOUNT_SUSPENDED(1209, "Account is suspended"),
    EMAIL_NOT_VERIFIED(1210, "Email not verified"),

    ADDRESS_NOT_FOUND(1300, "Address not found"),

    SESSION_NOT_FOUND(1400, "Session not found"),
    DEVICE_NOT_FOUND(1401, "Device not found"),
            ;
    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
