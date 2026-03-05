package com.example.autowebgenerator.ai.model.message;

import lombok.Getter;

/**
 * Type discriminator for unified SSE stream messages.
 *
 * <ul>
 *   <li>{@code ai_response}  — a partial AI text token (every onPartialResponse callback)</li>
 *   <li>{@code tool_executed} — a tool call that completed (writeFile finished for one file)</li>
 * </ul>
 */
@Getter
public enum StreamMessageTypeEnum {

    AI_RESPONSE("ai_response"),
    TOOL_EXECUTED("tool_executed");

    private final String value;

    StreamMessageTypeEnum(String value) {
        this.value = value;
    }
}
