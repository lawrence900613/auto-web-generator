package com.example.autowebgenerator.ai.model.message;

import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Emitted just before a tool executes (i.e., the AI has requested a tool call).
 *
 * JSON shape:
 * <pre>
 * {
 *   "type": "tool_request",
 *   "id":   "call_0",
 *   "name": "writeFile"
 * }
 * </pre>
 *
 * Used by the stream processing layer to emit a "selecting tool" indicator
 * in the chat history. The {@code seenToolIds} guard in AppServiceImpl ensures
 * each tool call is shown exactly once.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ToolRequestMessage extends StreamMessage {

    /** LangChain4j tool call ID, e.g. "call_0". */
    private String id;

    /** Tool method name, e.g. "writeFile". */
    private String name;

    public ToolRequestMessage(ToolExecution toolExecution) {
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        this.id = toolExecution.request().id();
        this.name = toolExecution.request().name();
    }
}
