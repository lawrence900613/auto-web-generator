package com.example.autowebgenerator.ai.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

/**
 * Signals that the AI has finished writing all files and generation is complete.
 *
 * Calling this tool causes LangChain4j to stop the agentic loop — no more
 * tool calls will be made after exit() returns.  This is more reliable than
 * prompt-level "STOP RULE" instructions because the model treats it as a
 * structured signal rather than free-form text to interpret.
 */
@Slf4j
public class ExitTool {

    @Tool("Call this tool once ALL project files have been written. It signals the end of code generation and must be the LAST tool call.")
    public String exit(@ToolMemoryId Long appId) {
        log.info("[ExitTool] app={} — generation complete", appId);
        return "Generation complete. Build will start now.";
    }
}
