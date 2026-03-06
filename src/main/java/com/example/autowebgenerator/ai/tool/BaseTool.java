package com.example.autowebgenerator.ai.tool;

import cn.hutool.json.JSONObject;

/**
 * Base class for all LangChain4j tools used in VUE_PROJECT generation.
 *
 * Implements the Strategy pattern — each subclass provides its own formatting
 * logic for tool request / execution display messages, allowing the stream
 * processing layer to stay generic instead of branching on tool names.
 */
public abstract class BaseTool {

    /** Returns the tool method name (matches the @Tool-annotated method's inferred name). */
    public abstract String getToolName();

    /** Returns the human-readable display name for this tool. */
    public abstract String getDisplayName();

    /**
     * Generates the string emitted when the AI selects this tool (before execution).
     * Default implementation shows "[Tool] {displayName}".
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[Tool] %s\n\n", getDisplayName());
    }

    /**
     * Generates the string emitted after this tool has executed.
     *
     * @param arguments the raw tool call arguments parsed as a JSONObject
     * @return formatted result string (may include markdown code blocks)
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);
}
