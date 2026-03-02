package com.example.autowebgenerator.exception;

import com.example.autowebgenerator.common.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleServiceExceptionShouldReturnBusinessError() {
        ServiceException exception = new ServiceException(ErrorCode.FORBIDDEN, "blocked");

        ApiResponse<?> response = handler.handleServiceException(exception);

        assertEquals(ErrorCode.FORBIDDEN.getCode(), response.getCode());
        assertEquals("blocked", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void handleRuntimeExceptionShouldReturnSystemError() {
        RuntimeException exception = new RuntimeException("boom");

        ApiResponse<?> response = handler.handleRuntimeException(exception);

        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), response.getCode());
        assertEquals("unexpected error", response.getMessage());
        assertNull(response.getData());
    }
}
