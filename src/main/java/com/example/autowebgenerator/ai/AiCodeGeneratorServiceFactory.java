package com.example.autowebgenerator.ai;

import com.example.autowebgenerator.ai.tool.ExitTool;
import com.example.autowebgenerator.ai.tool.FileDirReadTool;
import com.example.autowebgenerator.ai.tool.FileModifyTool;
import com.example.autowebgenerator.ai.tool.FileReadTool;
import com.example.autowebgenerator.ai.tool.FileWriteTool;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import com.example.autowebgenerator.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;

/**
 * Factory for per-app AI service instances with Caffeine caching.
 *
 * Cache key: "{appId}_{codeGenType.value}" — different code-gen modes for the
 * same app each get their own service instance (different tool configurations).
 *
 * Cache policy: max 1000 entries, expire 30 min after write, 10 min after last access.
 */
@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    /** No responseFormat constraint + 5-min timeout — used as sync model for VUE_PROJECT. */
    @Resource(name = "plainChatModel")
    private ChatModel plainChatModel;

    /** Standard streaming model — used for HTML / MULTI_FILE generation. */
    @Resource(name = "openAiStreamingChatModel")
    private StreamingChatModel openAiStreamingChatModel;

    /** Reasoning streaming model (o4-mini) — used for VUE_PROJECT generation. */
    @Resource(name = "reasoningStreamingChatModel")
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Lazy
    @Resource
    private ChatHistoryService chatHistoryService;

    /** AI service instance cache keyed by "{appId}_{codeGenType}" */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) ->
                    log.debug("AI service evicted from cache, key: {}, cause: {}", key, cause))
            .build();

    /**
     * Returns the cached AI service for the given appId and code-gen type,
     * creating one on cache miss.
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = appId + "_" + codeGenType.getValue();
        return serviceCache.get(cacheKey, k -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * Backward-compatible overload: defaults to HTML type.
     * Used by the default @Bean and any legacy callers.
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        log.info("Creating new AI service instance for appId: {}, type: {}", appId, codeGenType);

        String memoryId = appId + "_" + codeGenType.getValue();

        // Clear any stale Redis data (prevents old system messages from overriding @SystemMessage).
        // Safe because loadChatHistoryToMemory() re-hydrates from the DB right after.
        redisChatMemoryStore.deleteMessages(memoryId);

        // VUE_PROJECT needs a much larger window: each writeFile call adds 1 tool-result
        // message, so a 20-file project already occupies ~17 slots in round 1.  With only
        // 20 slots the oldest messages are evicted mid-generation, causing the AI to lose
        // context and start writing files again in a loop.
        int maxMessages = (codeGenType == CodeGenTypeEnum.VUE_PROJECT) ? 200 : 20;

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(maxMessages)
                .build();

        // Warm up Redis memory from DB history (lazy-load pattern)
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);

        // VUE_PROJECT uses plainChatModel (no responseFormat constraint) for sync fix calls.
        ChatModel syncModel = (codeGenType == CodeGenTypeEnum.VUE_PROJECT) ? plainChatModel : chatModel;

        return switch (codeGenType) {
            case VUE_PROJECT ->
                // FileWriteTool gets appId via @ToolMemoryId — no constructor injection needed.
                // chatMemoryProvider is required when @MemoryId is used on service methods.
                // hallucinatedToolNameStrategy prevents crashes when the AI invents tool names.
                AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(syncModel)
                        .streamingChatModel(reasoningStreamingChatModel)
                        .chatMemoryProvider(id -> chatMemory)
                        .tools(new FileWriteTool(), new ExitTool(),
                                new FileModifyTool(), new FileReadTool(), new FileDirReadTool())
                        .hallucinatedToolNameStrategy(req ->
                                ToolExecutionResultMessage.from(req,
                                        "Error: there is no tool called " + req.name()))
                        .build();
            default ->
                AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(syncModel)
                        .streamingChatModel(openAiStreamingChatModel)
                        .chatMemory(chatMemory)
                        .build();
        };
    }

    /** Default bean (appId=0, HTML type) kept for any direct AiCodeGeneratorService injections. */
    @Bean
    AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }
}
