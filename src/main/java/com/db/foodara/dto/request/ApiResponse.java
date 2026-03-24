package com.db.foodara.dto.request;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
