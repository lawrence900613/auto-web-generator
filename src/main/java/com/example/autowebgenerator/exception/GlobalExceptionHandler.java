package com.example.autowebgenerator.exception;

import com.example.autowebgenerator.common.ApiResponse;
import com.example.autowebgenerator.common.ApiResponseUtils;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    public ApiResponse<?> handleServiceException(ServiceException exception) {
        log.error("ServiceException", exception);
        return ApiResponseUtils.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException exception) {
        log.error("RuntimeException", exception);
        return ApiResponseUtils.fail(ErrorCode.SYSTEM_ERROR, "unexpected error");
    }
}
