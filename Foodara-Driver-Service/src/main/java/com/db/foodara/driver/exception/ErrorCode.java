package com.db.foodara.driver.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_KEY(1001, "Invalid Message Key"),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),

    // Auth
    UNAUTHENTICATED(1102, "Unauthenticated"),
    UNAUTHORIZED(1103, "You do not have permission"),

    // Driver
    DRIVER_NOT_FOUND(3001, "Driver not found"),
    DRIVER_DOCUMENT_NOT_FOUND(3014, "Driver document not found"),
    DRIVER_BANK_ACCOUNT_NOT_FOUND(3015, "Driver bank account not found"),
    DRIVER_SHIFT_NOT_FOUND(3016, "Driver shift not found"),
    INCENTIVE_NOT_FOUND(3010, "Incentive program not found"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
