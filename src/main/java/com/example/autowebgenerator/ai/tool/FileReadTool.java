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
 * Reads the content of a file in the generated project.
 *
 * Useful during auto-fix: the AI can read the current file content before
 * deciding exactly what to replace with modifyFile.
 */
@Slf4j
@Component
public class FileReadTool extends BaseTool {

    @Override
    public String getToolName() { return "readFile"; }

    @Override
    public String getDisplayName() { return "Read File"; }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String path = arguments.getStr("relativePath");
        return String.format("[Tool] %s %s", getDisplayName(), path);
    }

    @Tool("Read the current content of a file in the project. Use before modifyFile to verify the exact text to replace.")
    public String readFile(
            @ToolMemoryId Long appId,
            @P("Relative path of the file to read, e.g. 'src/App.vue'") String relativePath
    ) {
        String projectPath = VueProjectBuilder.projectPath(appId);
        String safe = relativePath.replaceAll("\\.\\./", "").replaceAll("^/+", "");
        File file = new File(projectPath + File.separator + safe.replace("/", File.separator));

        if (!file.exists() || !file.isFile()) {
            return "Error: file not found: " + safe;
        }
        log.info("[FileReadTool] app={} read: {}", appId, safe);
        return FileUtil.readString(file, StandardCharsets.UTF_8);
    }
}
