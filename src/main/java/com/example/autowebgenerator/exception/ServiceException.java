package com.example.autowebgenerator.exception;

public class ServiceException extends RuntimeException {

    private final int code;

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(ErrorCode error) {
        super(error.getMessage());
        this.code = error.getCode();
    }

    public ServiceException(ErrorCode error, String message) {
        super(message);
        this.code = error.getCode();
    }

    public int getCode() {
        return code;
    }
}
