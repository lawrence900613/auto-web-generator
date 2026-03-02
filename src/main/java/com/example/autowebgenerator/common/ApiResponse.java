package com.example.autowebgenerator.common;

import com.example.autowebgenerator.exception.ErrorCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * Unified API response wrapper.
 */
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code;
    private T data;
    private String message;

    public ApiResponse() {
    }

    public ApiResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public ApiResponse(int code, T data) {
        this(code, data, "");
    }

    public ApiResponse(ErrorCode error) {
        this(error.getCode(), null, error.getMessage());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
