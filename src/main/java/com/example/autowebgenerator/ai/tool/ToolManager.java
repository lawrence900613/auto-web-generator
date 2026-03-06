package com.example.autowebgenerator.ai.tool;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Centrally manages all registered BaseTool instances.
 *
 * Spring auto-injects all BaseTool beans into the tools array at startup.
 * The manager builds a name→tool map so callers can look up tools by name.
 *
 * NOTE: getAllTools() returns a BaseTool[] (array), not a Collection — this
 * is required by LangChain4j's AiServices.builder().tools() which only accepts
 * Object[] or individual Object varargs, not Collection types.
 */
@Slf4j
@Component
public class ToolManager {

    private final Map<String, BaseTool> toolMap = new HashMap<>();

    @Resource
    private BaseTool[] tools;

    @PostConstruct
    public void initTools() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("Registered tool: {} -> {}", tool.getToolName(), tool.getDisplayName());
        }
        log.info("ToolManager initialized with {} tools", toolMap.size());
    }

    /** Returns the tool instance for the given tool method name, or null if not found. */
    public BaseTool getTool(String toolName) {
        return toolMap.get(toolName);
    }

    /** Returns all registered tool instances (used by AiServices.builder().tools()). */
    public BaseTool[] getAllTools() {
        return tools;
    }
}
