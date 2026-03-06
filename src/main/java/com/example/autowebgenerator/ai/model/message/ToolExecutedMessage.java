package com.example.autowebgenerator.ai.model.message;

import cn.hutool.json.JSONUtil;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emitted after a tool call completes (i.e., FileWriteTool.writeFile() returned).
 *
 * JSON shape:
 * <pre>
 * {
 *   "type": "tool_executed",
 *   "id":   "call_0",
 *   "name": "writeFile",
 *   "path": "src/App.vue",
 *   "content": "<template>...</template>",
 *   "result": "OK: src/App.vue (saved to disk...)"
 * }
 * </pre>
 *
 * {@code path} and {@code content} are parsed from the tool's {@code arguments} JSON so
 * the frontend can render a proper code block without an extra round-trip.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ToolExecutedMessage extends StreamMessage {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutedMessage.class);

    /** LangChain4j tool call ID, e.g. "call_0". */
    private String id;

    /** Tool name — always "writeFile" for VUE_PROJECT. */
    private String name;

    /** Relative file path within the project root, e.g. "src/App.vue". */
    private String path;

    /** Full file content as written to disk. */
    private String content;

    /** Raw return value from the tool, e.g. "OK: src/App.vue (saved to disk...)". */
    private String result;

    /** Raw arguments JSON string — used by ToolManager.generateToolExecutedResult(). */
    private String arguments;

    public ToolExecutedMessage(ToolExecution toolExecution) {
        super(StreamMessageTypeEnum.TOOL_EXECUTED.getValue());
        this.id = toolExecution.request().id();
        this.name = toolExecution.request().name();
        this.result = toolExecution.result();
        this.arguments = toolExecution.request().arguments();

        // Parse arguments JSON: {"relativePath": "...", "content": "..."}
        try {
            var args = JSONUtil.parseObj(toolExecution.request().arguments());
            this.path = args.getStr("relativePath");
            this.content = args.getStr("content");
        } catch (Exception e) {
            log.warn("ToolExecutedMessage: failed to parse arguments for tool '{}': {} | args={}",
                    this.name, e.getMessage(), toolExecution.request().arguments());
            // Fallback: extract path from the result string
            String r = toolExecution.result();
            this.path = r.startsWith("OK: ") ? r.substring(4).split(" ")[0] : r;
            this.content = "";
        }
    }
}
