package com.db.foodara.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    /**
     * Optional contextual detail (e.g. names of out-of-stock items) that
     * gets appended to the user-facing message.
     */
    private final String detail;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public AppException(ErrorCode errorCode, String detail) {
        super(detail != null && !detail.isBlank()
                ? errorCode.getMessage() + ": " + detail
                : errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
