package com.example.autowebgenerator.controller;

import com.example.autowebgenerator.common.ApiResponse;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ServiceException;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Legacy REST API for AI code generation.
 *
 * Base path: /api/ai/generate
 *
 * NOTE:
 * - Temporarily restricted to vue_project mode only.
 * - This legacy controller is disabled for runtime generation.
 * - Use /api/app/chat/gen/code?appId=... for active vue_project generation.
 */
@RestController
@RequestMapping("/ai/generate")
public class AiCodeGeneratorController {

    public record GenerateRequest(String message, String type) {}

    @PostMapping
    public ApiResponse<String> generate(@RequestBody GenerateRequest request) {
        validate(request.message(), request.type());
        throw new ServiceException(
                ErrorCode.BAD_REQUEST,
                "Legacy synchronous endpoint is temporarily disabled. Use /app/chat/gen/code with appId.");
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStream(
            @RequestParam String message,
            @RequestParam(defaultValue = "vue_project") String type) {
        validate(message, type);
        throw new ServiceException(
                ErrorCode.BAD_REQUEST,
                "Legacy stream endpoint is temporarily disabled. Use /app/chat/gen/code with appId.");
    }

    private void validate(String message, String type) {
        if (message == null || message.isBlank()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "message must not be blank");
        }
        if (CodeGenTypeEnum.fromValue(type) == null) {
            throw new ServiceException(ErrorCode.BAD_REQUEST,
                    "type must be: vue_project - got: " + type);
        }
    }
}
