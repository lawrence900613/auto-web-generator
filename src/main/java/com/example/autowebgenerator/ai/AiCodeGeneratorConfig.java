package com.example.autowebgenerator.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manual Spring configuration for LangChain4j + OpenAI.
 *
 * Why manual instead of the LangChain4j Spring Boot starter?
 * The starter currently targets Spring Boot 3.x; this project uses Spring Boot 4.x.
 * Wiring the beans by hand keeps us compatible with any Spring Boot version.
 *
 * Bean overview:
 *   chatModel           → OpenAI model used for synchronous structured-output calls
 *   streamingChatModel  → OpenAI model used for token-by-token SSE streaming
 *   aiCodeGeneratorService → LangChain4j proxy that implements AiCodeGeneratorService
 */
@Configuration
public class AiCodeGeneratorConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String modelName;

    @Value("${openai.max-tokens:8192}")
    private int maxTokens;

    @Value("${openai.log-requests:false}")
    private boolean logRequests;

    @Value("${openai.log-responses:false}")
    private boolean logResponses;

    /**
     * Synchronous ChatModel — used for structured JSON output.
     *
     * responseFormat("json_object") tells OpenAI to always return valid JSON,
     * which LangChain4j then deserialises into HtmlCodeResult / MultiFileCodeResult.
     */
    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .responseFormat("json_object")   // ensures the AI outputs valid JSON
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }

    /**
     * Streaming ChatModel — used for real-time SSE token delivery.
     *
     * Does NOT use json_object mode — the AI streams raw markdown with code
     * blocks that our CodeParser extracts after the stream completes.
     */
    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }

    /**
     * AiCodeGeneratorService proxy created by LangChain4j's AiServices.
     *
     * The proxy reads @SystemMessage annotations from AiCodeGeneratorService,
     * loads the prompt .txt files from the classpath, calls the appropriate
     * model (chatModel for POJO returns, streamingChatModel for TokenStream),
     * and handles JSON deserialisation automatically.
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService(ChatModel chatModel,
                                                         StreamingChatModel streamingChatModel) {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
