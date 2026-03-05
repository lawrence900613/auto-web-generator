package com.example.autowebgenerator.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiCodeGeneratorConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String modelName;

    @Value("${openai.reasoning-model:o4-mini}")
    private String reasoningModelName;

    @Value("${openai.max-tokens:8192}")
    private int maxTokens;

    @Value("${openai.log-requests:false}")
    private boolean logRequests;

    @Value("${openai.log-responses:false}")
    private boolean logResponses;

    /**
     * Synchronous ChatModel — used for structured JSON output (HTML / MULTI_FILE).
     * responseFormat("json_object") tells OpenAI to always return valid JSON.
     */
    @Bean
    ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .responseFormat("json_object")
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }

    /**
     * Standard streaming ChatModel — used for HTML / MULTI_FILE SSE streaming.
     * Renamed to openAiStreamingChatModel to avoid conflict with reasoningStreamingChatModel.
     */
    @Bean("openAiStreamingChatModel")
    StreamingChatModel openAiStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }

    /**
     * Reasoning streaming ChatModel — used exclusively for VUE_PROJECT generation.
     *
     * o4-mini is OpenAI's fast reasoning model with excellent coding ability and
     * native tool-calling support. No responseFormat or maxTokens constraints —
     * reasoning models manage their own output length via max_completion_tokens.
     */
    @Bean("reasoningStreamingChatModel")
    StreamingChatModel reasoningStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(reasoningModelName)
                .timeout(Duration.ofMinutes(5))
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }

    /**
     * Plain synchronous ChatModel — no responseFormat constraint, extended timeout.
     * Used for VUE_PROJECT sync calls (fixVueProjectCode).
     */
    @Bean("plainChatModel")
    ChatModel plainChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .timeout(Duration.ofMinutes(5))
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}
