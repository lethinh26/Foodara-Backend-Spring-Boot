package com.db.foodara.exception;

public enum ErrorCode {
    PAYMENT_FAILED(1001, "Payment failed", 400);

    private final int code;
    private final String message;
    private final int httpStatusCode;

    ErrorCode(int code, String message, int httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
