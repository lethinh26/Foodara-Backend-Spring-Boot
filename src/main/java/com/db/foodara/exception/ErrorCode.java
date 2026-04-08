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

    // merchant
    MERCHANT_NAME_INVALID(1500, "Merchant name isn't valid"),
    TAX_CODE_INVALID(1501, "Tax code isn't valid"),
    MERCHANT_NOT_FOUND(1502, "Merchant not found"),
    MERCHANT_EMAIL_INVALID(1503, "Merchant's email isn't valid"),
    MERCHANT_PHONE_INVALID(1504, "Merchant's phone isn't valid"),
    MERCHANT_ID_REQUIRED(1602, "Merchant ID is required"),


    // store
    STORE_NAME_INVALID(1600, "Store's name isn't valid"),
    STORE_NOT_FOUND(1601, "Store not found"),
    STORE_NAME_REQUIRED(1603, "Store name is required"),
    SLUG_REQUIRED(1604, "Slug is required"),
    SLUG_INVALID_FORMAT(1602, "Slug format is invalid"),
    DISTRICT_REQUIRED(1602, "District is required"),
    CITY_REQUIRED(1602, "City ID is required"),

    // document
    DOCUMENT_NOT_FOUND(1700, "Document not found"),
    DOCUMENT_INVALID(1701, "Document is invalid"),

    // bank account
    BANK_ACCOUNT_NOT_FOUND(1800, "Bank Account not found"),
    BANK_NAME_REQUIRED(1801, "Bank name is required"),
    ACCOUNT_NUMBER_REQUIRED(1802, "Bank number is required"),
    BANK_ACCOUNT_ALREADY_EXISTS(1803, "Bank account is existed"),

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
