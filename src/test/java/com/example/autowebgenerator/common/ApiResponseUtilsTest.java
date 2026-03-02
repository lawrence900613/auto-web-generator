package com.example.autowebgenerator.common;

import com.example.autowebgenerator.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiResponseUtilsTest {

    @Test
    void successWithDataShouldFillCodeMessageAndData() {
        ApiResponse<String> response = ApiResponseUtils.success("value");

        assertEquals(ErrorCode.OK.getCode(), response.getCode());
        assertEquals(ErrorCode.OK.getMessage(), response.getMessage());
        assertEquals("value", response.getData());
    }

    @Test
    void failWithErrorCodeShouldUseErrorCodeAndMessage() {
        ApiResponse<Void> response = ApiResponseUtils.fail(ErrorCode.BAD_REQUEST);

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.getMessage());
        assertNull(response.getData());
    }
}
