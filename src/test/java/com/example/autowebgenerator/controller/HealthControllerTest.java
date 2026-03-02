package com.example.autowebgenerator.controller;

import com.example.autowebgenerator.common.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Test
    void healthCheckShouldReturnOkResponse() {
        ApiResponse<String> response = healthController.healthCheck();

        assertEquals(0, response.getCode());
        assertEquals("ok", response.getMessage());
        assertEquals("ok", response.getData());
    }
}
