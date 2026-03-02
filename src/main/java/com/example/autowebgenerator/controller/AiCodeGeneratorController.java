package com.example.autowebgenerator.controller;

import com.example.autowebgenerator.common.ApiResponse;
import com.example.autowebgenerator.common.ApiResponseUtils;
import com.example.autowebgenerator.core.AiCodeGeneratorFacade;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ServiceException;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * REST API for AI web code generation.
 *
 * Base path: /api/ai/generate
 *
 * Endpoints:
 *   POST /api/ai/generate        — synchronous, returns saved directory path
 *   GET  /api/ai/generate/stream — SSE streaming, pushes tokens as they arrive
 *
 * Quick test with curl:
 *
 *   # Synchronous HTML generation
 *   curl -X POST http://localhost:8123/api/ai/generate \
 *        -H "Content-Type: application/json" \
 *        -d '{"message":"A personal portfolio website","type":"html"}'
 *
 *   # Streaming multi-file generation
 *   curl -N "http://localhost:8123/api/ai/generate/stream?message=A+todo+app&type=multi_file"
 */
@RestController
@RequestMapping("/ai/generate")
public class AiCodeGeneratorController {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    // -------------------------------------------------------------------------
    // Request DTO
    // -------------------------------------------------------------------------

    public record GenerateRequest(String message, String type) {}

    // -------------------------------------------------------------------------
    // Synchronous endpoint
    // -------------------------------------------------------------------------

    /**
     * Generate a website synchronously.
     *
     * The AI call blocks until the full response is received, then saves the
     * files and returns the output directory path.
     *
     * Typical latency: 10–60 seconds depending on model and prompt complexity.
     * Use the streaming endpoint for better UX on long generations.
     *
     * @param request  { "message": "...", "type": "html" | "multi_file" }
     * @return         { "code": 0, "data": "/absolute/path/to/output/dir" }
     */
    @PostMapping
    public ApiResponse<String> generate(@RequestBody GenerateRequest request) {
        validate(request.message(), request.type());

        CodeGenTypeEnum type = CodeGenTypeEnum.fromValue(request.type());
        File outputDir = aiCodeGeneratorFacade.generateAndSave(request.message(), type);

        return ApiResponseUtils.success(outputDir.getAbsolutePath());
    }

    // -------------------------------------------------------------------------
    // Streaming endpoint (SSE)
    // -------------------------------------------------------------------------

    /**
     * Generate a website with real-time SSE streaming.
     *
     * The browser / curl receives each token as it is produced by the model.
     * Files are saved automatically once the stream completes.
     *
     * @param message  natural-language description of the website
     * @param type     "html" or "multi_file"
     * @return         Flux<String> — Spring MVC writes each token as an SSE event
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStream(
            @RequestParam String message,
            @RequestParam(defaultValue = "html") String type) {

        validate(message, type);
        CodeGenTypeEnum codeGenType = CodeGenTypeEnum.fromValue(type);
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenType);
    }

    // -------------------------------------------------------------------------
    // Validation helper
    // -------------------------------------------------------------------------

    private void validate(String message, String type) {
        if (message == null || message.isBlank()) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "message must not be blank");
        }
        if (CodeGenTypeEnum.fromValue(type) == null) {
            throw new ServiceException(ErrorCode.BAD_REQUEST,
                    "type must be one of: html, multi_file — got: " + type);
        }
    }
}
