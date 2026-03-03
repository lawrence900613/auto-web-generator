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

@Service
public class AiCodeGeneratorFacade {

    private static final Logger log = LoggerFactory.getLogger(AiCodeGeneratorFacade.class);

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    // -------------------------------------------------------------------------
    // Synchronous generation
    // -------------------------------------------------------------------------

    public File generateAndSave(String userMessage, CodeGenTypeEnum type) {
        return switch (type) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaver.saveHtmlCode(result);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaver.saveMultiFileCode(result);
            }
        };
    }

    // -------------------------------------------------------------------------
    // Streaming — anonymous (no appId)
    // -------------------------------------------------------------------------

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum type) {
        if (type == null) throw new ServiceException(ErrorCode.SYSTEM_ERROR, "type must not be null");
        return switch (type) {
            case HTML       -> streamHtml(userMessage, null);
            case MULTI_FILE -> streamMultiFile(userMessage, null);
        };
    }

    // -------------------------------------------------------------------------
    // Streaming — per-app (saves to tmp/code_output/{appId}/)
    // -------------------------------------------------------------------------

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum type, Long appId) {
        if (type == null) throw new ServiceException(ErrorCode.SYSTEM_ERROR, "type must not be null");
        return switch (type) {
            case HTML       -> streamHtml(userMessage, appId);
            case MULTI_FILE -> streamMultiFile(userMessage, appId);
        };
    }

    // -------------------------------------------------------------------------
    // Private streaming helpers
    // -------------------------------------------------------------------------

    private Flux<String> streamHtml(String userMessage, Long appId) {
        StringBuilder buf = new StringBuilder();
        return aiCodeGeneratorService.generateHtmlCodeStream(userMessage)
                .doOnNext(buf::append)
                .doOnComplete(() -> {
                    try {
                        HtmlCodeResult result = com.example.autowebgenerator.core.CodeParser.parseHtmlCode(buf.toString());
                        if (appId != null) {
                            CodeFileSaver.saveHtmlCodeForApp(result, appId);
                        } else {
                            CodeFileSaver.saveHtmlCode(result);
                        }
                    } catch (Exception e) {
                        log.error("Failed to save streamed HTML code", e);
                    }
                });
    }

    private Flux<String> streamMultiFile(String userMessage, Long appId) {
        StringBuilder buf = new StringBuilder();
        return aiCodeGeneratorService.generateMultiFileCodeStream(userMessage)
                .doOnNext(buf::append)
                .doOnComplete(() -> {
                    try {
                        MultiFileCodeResult result = CodeParser.parseMultiFileCode(buf.toString());
                        if (appId != null) {
                            CodeFileSaver.saveMultiFileCodeForApp(result, appId);
                        } else {
                            CodeFileSaver.saveMultiFileCode(result);
                        }
                    } catch (Exception e) {
                        log.error("Failed to save streamed multi-file code", e);
                    }
                });
    }
}
