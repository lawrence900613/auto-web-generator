package com.example.autowebgenerator.core;

import cn.hutool.json.JSONUtil;
import com.example.autowebgenerator.ai.AiCodeGeneratorService;
import com.example.autowebgenerator.ai.AiCodeGeneratorServiceFactory;
import com.example.autowebgenerator.ai.model.message.AiResponseMessage;
import com.example.autowebgenerator.ai.model.message.ToolExecutedMessage;
import com.example.autowebgenerator.ai.model.message.ToolRequestMessage;
import com.example.autowebgenerator.core.builder.VueProjectBuilder;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ServiceException;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.File;

@Service
public class AiCodeGeneratorFacade {

    private static final Logger log = LoggerFactory.getLogger(AiCodeGeneratorFacade.class);

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    // -------------------------------------------------------------------------
    // Synchronous generation (legacy endpoint path)
    // -------------------------------------------------------------------------

    public File generateAndSave(String userMessage, CodeGenTypeEnum type) {
        throw new ServiceException(
                ErrorCode.BAD_REQUEST,
                "Legacy synchronous generation is temporarily disabled. Use app chat generation endpoint.");
    }

    // -------------------------------------------------------------------------
    // Streaming — anonymous (no appId, uses default service)
    // -------------------------------------------------------------------------

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum type) {
        if (type == null) throw new ServiceException(ErrorCode.SYSTEM_ERROR, "type must not be null");
        if (type != CodeGenTypeEnum.VUE_PROJECT) {
            // Legacy modes are temporarily disabled:
            // - HTML
            // - MULTI_FILE
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Only vue_project mode is supported.");
        }
        throw new ServiceException(
                ErrorCode.BAD_REQUEST,
                "Anonymous stream is disabled for vue_project. Use app-scoped stream with appId.");
    }

    // -------------------------------------------------------------------------
    // Streaming — per-app (uses per-appId service with Redis chat memory)
    // -------------------------------------------------------------------------

    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum type, Long appId) {
        if (type == null) throw new ServiceException(ErrorCode.SYSTEM_ERROR, "type must not be null");
        return switch (type) {
            case VUE_PROJECT -> streamVueProject(userMessage, appId);
            // Temporarily disabled modes:
            // case HTML       -> streamHtml(userMessage, appId);
            // case MULTI_FILE -> streamMultiFile(userMessage, appId);
            case HTML, MULTI_FILE ->
                    throw new ServiceException(ErrorCode.BAD_REQUEST, "Only vue_project mode is supported.");
        };
    }

    // -------------------------------------------------------------------------
    // Private streaming helpers
    // -------------------------------------------------------------------------

    /** Emit a typed AI text token as JSON. */
    private static String aiChunk(String text) {
        return JSONUtil.toJsonStr(new AiResponseMessage(text));
    }

    // Legacy stream helpers are intentionally commented out while only vue_project is supported.
//    private Flux<String> streamHtml(String userMessage, Long appId) { ... }
//    private Flux<String> streamMultiFile(String userMessage, Long appId) { ... }

    /**
     * VUE_PROJECT generation uses streaming + FileWriteTool.
     *
     * The AI streams its plan as AI_RESPONSE chunks (forwarded to the SSE client),
     * then calls writeFile() for each file — LangChain4j executes the tool automatically
     * and fires onToolExecuted, which emits a TOOL_EXECUTED chunk.
     * After all files are written, onCompleteResponse fires and npm build runs in a
     * daemon thread so the SSE connection stays alive until the build finishes.
     *
     * On build failure: fixVueProjectCode() sends the error back to the AI, which calls
     * writeFile() again for any broken files, then we rebuild.
     */
    private Flux<String> streamVueProject(String userMessage, Long appId) {
        if (appId == null) throw new ServiceException(ErrorCode.BAD_REQUEST, "appId is required for VUE_PROJECT");
        return Flux.create(emitter -> {
            AiCodeGeneratorService service = aiCodeGeneratorServiceFactory
                    .getAiCodeGeneratorService(appId, CodeGenTypeEnum.VUE_PROJECT);
            TokenStream tokenStream = service.generateVueProjectCodeStream(appId, userMessage);
            tokenStream
                    .onPartialResponse(token -> emitter.next(aiChunk(token)))
                    .onToolExecuted(toolExecution -> {
                            emitter.next(JSONUtil.toJsonStr(new ToolRequestMessage(toolExecution)));
                            emitter.next(JSONUtil.toJsonStr(new ToolExecutedMessage(toolExecution)));
                    })
                    .onCompleteResponse(response -> {
                        emitter.next(aiChunk("\n\nBuilding Vue project (npm install + npm run build)...\n"));
                        Thread buildThread = new Thread(() -> {
                            String projectPath = VueProjectBuilder.projectPath(appId);
                            // Send heartbeat every 5 s so the SSE connection stays alive
                            // during the potentially long npm install + build phase.
                            java.util.concurrent.atomic.AtomicBoolean buildDone =
                                    new java.util.concurrent.atomic.AtomicBoolean(false);
                            Thread heartbeat = new Thread(() -> {
                                int elapsed = 0;
                                while (!buildDone.get()) {
                                    try { Thread.sleep(5000); } catch (InterruptedException ie) { break; }
                                    if (!buildDone.get()) {
                                        elapsed += 5;
                                        emitter.next(aiChunk("Building... " + elapsed + "s elapsed\n"));
                                    }
                                }
                            });
                            heartbeat.setDaemon(true);
                            heartbeat.start();
                            try {
                                vueProjectBuilder.buildProject(projectPath);
                                buildDone.set(true);
                                emitter.next(aiChunk("Build complete! Your Vue app is ready to preview.\n"));
                                emitter.complete();
                            } catch (Exception buildErr) {
                                buildDone.set(true);
                                log.warn("VUE_PROJECT build failed (attempt 1) for app {} — trying auto-fix", appId);
                                emitter.next(aiChunk("\nBuild error detected. Asking AI to fix the code...\n"));
                                try {
                                    String fixPrompt = "The Vite build failed with the error below.\n" +
                                            "1. Use readFile to inspect the broken file(s).\n" +
                                            "2. Use modifyFile to fix ONLY the broken section (preferred), or writeFile to replace the whole file.\n" +
                                            "3. Call exit() when all fixes are applied.\n\n" +
                                            buildErr.getMessage();
                                    service.fixVueProjectCode(appId, fixPrompt);
                                    emitter.next(aiChunk("Applying fixes and rebuilding...\n"));
                                    // New heartbeat for the retry build
                                    java.util.concurrent.atomic.AtomicBoolean retryDone =
                                            new java.util.concurrent.atomic.AtomicBoolean(false);
                                    Thread retryHeartbeat = new Thread(() -> {
                                        int e2 = 0;
                                        while (!retryDone.get()) {
                                            try { Thread.sleep(5000); } catch (InterruptedException ie) { break; }
                                            if (!retryDone.get()) {
                                                e2 += 5;
                                                emitter.next(aiChunk("Rebuilding... " + e2 + "s elapsed\n"));
                                            }
                                        }
                                    });
                                    retryHeartbeat.setDaemon(true);
                                    retryHeartbeat.start();
                                    vueProjectBuilder.buildProject(projectPath);
                                    retryDone.set(true);
                                    emitter.next(aiChunk("Build complete! Your Vue app is ready to preview.\n"));
                                    emitter.complete();
                                } catch (Exception retryErr) {
                                    log.error("VUE_PROJECT auto-fix failed for app {}", appId, retryErr);
                                    emitter.error(retryErr);
                                }
                            }
                        });
                        buildThread.setDaemon(true);
                        buildThread.start();
                    })
                    .onError(emitter::error)
                    .start();
        }, FluxSink.OverflowStrategy.BUFFER);
    }
}
