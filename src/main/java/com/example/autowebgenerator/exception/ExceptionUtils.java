package com.example.autowebgenerator.exception;

public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static void throwIf(boolean condition, RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }

    public static void throwIf(boolean condition, ErrorCode error) {
        throwIf(condition, new ServiceException(error));
    }

    public static void throwIf(boolean condition, ErrorCode error, String message) {
        throwIf(condition, new ServiceException(error, message));
    }
}
