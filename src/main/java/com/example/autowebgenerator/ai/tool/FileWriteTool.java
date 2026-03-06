package com.example.autowebgenerator.ai.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.example.autowebgenerator.core.builder.VueProjectBuilder;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * LangChain4j tool that writes files to the Vue project directory.
 *
 * The project path is derived at call time from {@code @ToolMemoryId Long appId},
 * which LangChain4j automatically passes from the {@code @MemoryId} on the calling
 * service method — no constructor injection required.
 */
@Slf4j
@Component
public class FileWriteTool extends BaseTool {

    @Override
    public String getToolName() { return "writeFile"; }

    @Override
    public String getDisplayName() { return "Write File"; }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String path = arguments.getStr("relativePath");
        String suffix = FileUtil.getSuffix(path);
        String content = arguments.getStr("content");
        return String.format("[Tool] %s %s\n```%s\n%s\n```", getDisplayName(), path, suffix, content);
    }

    @Tool("Write a single file to the Vue project. Call this once for every file in the project.")
    public String writeFile(
            @ToolMemoryId Long appId,
            @P("Relative path within the project root, e.g. 'package.json' or 'src/main.js'")
            String relativePath,
            @P("Complete content of the file")
            String content
    ) {
        String projectPath = VueProjectBuilder.projectPath(appId);
        // Sanitise: strip leading slashes / path traversal
        String safe = relativePath.replaceAll("\\.\\./", "").replaceAll("^/+", "");
        String fullPath = projectPath + File.separator + safe.replace("/", File.separator);
        FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);
        log.info("[FileWriteTool] app={} wrote: {}", appId, safe);
        return "OK: " + safe + " (saved to disk — do NOT write this file again)";
    }
}
