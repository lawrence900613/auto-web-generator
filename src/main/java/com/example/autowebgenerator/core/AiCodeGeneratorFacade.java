package com.example.autowebgenerator.core;

import com.example.autowebgenerator.ai.AiCodeGeneratorService;
import com.example.autowebgenerator.ai.model.HtmlCodeResult;
import com.example.autowebgenerator.ai.model.MultiFileCodeResult;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ServiceException;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * Facade (entry point) for all AI code generation operations.
 *
 * Two generation modes are supported:
 *
 *   generateAndSave(...)           — synchronous, structured JSON output
 *   generateAndSaveCodeStream(...) — Flux streaming, markdown parsing on completion
 *
 * Flow diagram:
 *
 *   [Client]
 *      ↓
 *   AiCodeGeneratorFacade
 *      ├─ sync   → AiCodeGeneratorService.generateXxx()       → CodeFileSaver.saveXxx()
 *      └─ stream → AiCodeGeneratorService.generateXxxStream() (Flux<String>)
 *                      ↓ tokens via doOnNext
 *                  StringBuilder accumulates
 *                      ↓ doOnComplete
 *                  CodeParser.parseXxx()
 *                      ↓
 *                  CodeFileSaver.saveXxx()
 */
@Service
public class AiCodeGeneratorFacade {

    private static final Logger log = LoggerFactory.getLogger(AiCodeGeneratorFacade.class);

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    // =========================================================================
    // Synchronous generation
    // =========================================================================

    public File generateAndSave(String userMessage, CodeGenTypeEnum type) {
        return switch (type) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                log.info("HTML generation complete: {}", result);
                yield CodeFileSaver.saveHtmlCode(result);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                log.info("Multi-file generation complete: {}", result);
                yield CodeFileSaver.saveMultiFileCode(result);
            }
        };
    }

    // =========================================================================
    // Streaming generation (Flux / SSE)
    // =========================================================================

    /**
     * Unified streaming entry point — dispatches to the correct private helper
     * based on the generation type.
     *
     * Each emitted String is one token from the model. The caller (controller)
     * can forward these directly to the HTTP response as SSE events.
     * Files are saved automatically once the stream completes.
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum type) {
        if (type == null) {
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "type must not be null");
        }
        return switch (type) {
            case HTML      -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
        };
    }

    // =========================================================================
    // Private streaming helpers
    // =========================================================================

    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        StringBuilder codeBuilder = new StringBuilder();
        return aiCodeGeneratorService.generateHtmlCodeStream(userMessage)
                .doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    try {
                        HtmlCodeResult result = CodeParser.parseHtmlCode(codeBuilder.toString());
                        File savedDir = CodeFileSaver.saveHtmlCode(result);
                        log.info("Stream save complete → {}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("Failed to save streamed HTML code", e);
                    }
                });
    }

    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        StringBuilder codeBuilder = new StringBuilder();
        return aiCodeGeneratorService.generateMultiFileCodeStream(userMessage)
                .doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    try {
                        MultiFileCodeResult result = CodeParser.parseMultiFileCode(codeBuilder.toString());
                        File savedDir = CodeFileSaver.saveMultiFileCode(result);
                        log.info("Stream save complete → {}", savedDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("Failed to save streamed multi-file code", e);
                    }
                });
    }
}
