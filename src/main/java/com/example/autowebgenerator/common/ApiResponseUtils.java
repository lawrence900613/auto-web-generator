package com.example.autowebgenerator.common;

import com.example.autowebgenerator.exception.ErrorCode;

public final class ApiResponseUtils {

    private ApiResponseUtils() {
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.OK.getCode(), data, ErrorCode.OK.getMessage());
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(ErrorCode.OK.getCode(), null, ErrorCode.OK.getMessage());
    }

    public static ApiResponse<Void> fail(ErrorCode error) {
        return new ApiResponse<>(error);
    }

    public static ApiResponse<Void> fail(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }

    public static ApiResponse<Void> fail(ErrorCode error, String message) {
        return new ApiResponse<>(error.getCode(), null, message);
    }
}
