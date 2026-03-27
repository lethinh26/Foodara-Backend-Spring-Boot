package com.db.foodara.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_KEY(1001, "Invalid Message Key"),
    USER_EXISTED(1002, "User existed"),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    USERNAME_INVALID(1003, "Username must be at least 3 characters"),
    PASSWORD_INVALID(1004, "Password must be at least 8 characters"),
    EMAIL_INVALID(1005, "Email isn't valid"),
    PHONE_INVALID(1006, "Phone isn't valid"),

    ;
    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
