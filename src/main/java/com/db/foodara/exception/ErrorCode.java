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
    USERNAME_INVALID(1204, "Username must be at least 8 characters"),
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
    MERCHANT_OWNER_NOT_FOUND(1502, "Merchant not found"),
    MERCHANT_EMAIL_INVALID(1503, "Merchant's email isn't valid"),
    MERCHANT_PHONE_INVALID(1504, "Merchant's phone isn't valid"),
    MERCHANT_ID_REQUIRED(1602, "Merchant ID is required"),


    // store
    STORE_NAME_INVALID(1600, "Store's name isn't valid"),
    STORE_BASE_NOT_FOUND(1601, "Store not found"),
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

    // Location
    CITY_NOT_FOUND(1500, "City not found"),
    DISTRICT_NOT_FOUND(1501, "District not found"),
    SERVICE_ZONE_NOT_FOUND(1502, "Service zone not found"),
    LOCATION_NOT_COVERED(1503, "Location is not within service coverage"),
    GEOCODING_FAILED(1504, "Geocoding failed"),
    INVALID_COORDINATES(1505, "Invalid latitude or longitude"),
    DISTRICT_NOT_IN_CITY(1506, "District does not belong to specified city"),

    // Store & Categories
    STORE_CATEGORY_EXISTED(1600, "Store category already existed"),
    STORE_CATEGORY_NOT_FOUND(1601, "Store category not found"),
    STORE_NOT_FOUND(1602, "Store not found"),
    MENU_CATEGORY_NAME_EXISTED(1603, "Name of Menu category is existed"),
    MENU_CATEGORY_NOT_FOUND(1603, "Menu category is not found"),
    MENU_ITEM_NAME_INVALID(1604, "Menu category name is invalid"),
    MENU_ITEM_NOT_FOUND(1605, "Menu item not found"),
    OPTION_GROUP_NOT_FOUND(1606, "Option group not found"),
    COMBO_ITEMS_REQUIRED(1607, "Combo items is required"),
    COMBO_NOT_FOUND(1608, "Combo is not founded"),

    // Merchant
    MERCHANT_NOT_FOUND(1700, "Merchant not found"),
    MERCHANT_ALREADY_EXISTS(1701, "Merchant already exists for this user"),
    MERCHANT_DOCUMENT_NOT_FOUND(1702, "Document not found"),
    MERCHANT_BANK_ACCOUNT_NOT_FOUND(1703, "Bank account not found"),


    // order
    ORDER_NOT_FOUND(1800, "Order not found"),
    WRONG_ORDER(1801, "Wrong store order of other owner"),

    // cart
    CART_NOT_FOUND(1900, "Cart not found"),
    CART_ITEM_NOT_FOUND(1901, "Cart item not found"),
    CART_INVALID_REQUEST(1902, "Cart request is invalid"),
    CART_INVALID_OPTION(1903, "Cart item options are invalid"),
    CART_ITEM_UNAVAILABLE(1904, "Cart item is unavailable"),
    CART_STORE_MISMATCH(1905, "Cart item does not belong to selected store"),

    // voucher
    VOUCHER_NOT_FOUND(2000, "Voucher not found"),
    VOUCHER_NOT_ACTIVE(2001, "Voucher is not active"),
    VOUCHER_NOT_ELIGIBLE(2002, "Voucher is not eligible for this order"),
    VOUCHER_OUT_OF_STOCK(2003, "Voucher has run out")

    ;
    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
