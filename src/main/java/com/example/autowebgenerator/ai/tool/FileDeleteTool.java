package com.example.autowebgenerator.ai.tool;

import cn.hutool.json.JSONObject;
import com.example.autowebgenerator.core.builder.VueProjectBuilder;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

/**
 * Deletes a single file from the generated Vue project.
 *
 * Important files (package.json, vite.config.js, etc.) are protected and
 * cannot be deleted to prevent accidental project breakage.
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool {

    private static final Set<String> PROTECTED_FILES = Set.of(
            "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
            "vite.config.js", "vite.config.ts", "vue.config.js",
            "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
            "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
    );

    @Override
    public String getToolName() { return "deleteFile"; }

    @Override
    public String getDisplayName() { return "Delete File"; }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String path = arguments.getStr("relativePath");
        return String.format("[Tool] %s %s", getDisplayName(), path);
    }

    @Tool("Delete a single file from the project. Cannot delete protected files like package.json, index.html, etc.")
    public String deleteFile(
            @ToolMemoryId Long appId,
            @P("Relative path of the file to delete, e.g. 'src/components/OldComponent.vue'") String relativePath
    ) {
        String projectPath = VueProjectBuilder.projectPath(appId);
        String safe = relativePath.replaceAll("\\.\\./", "").replaceAll("^/+", "");
        File file = new File(projectPath + File.separator + safe.replace("/", File.separator));

        if (!file.exists()) {
            return "Warning: file does not exist, nothing to delete - " + safe;
        }
        if (!file.isFile()) {
            return "Error: path is not a file - " + safe;
        }

        String fileName = file.getName();
        if (PROTECTED_FILES.stream().anyMatch(p -> p.equalsIgnoreCase(fileName))) {
            return "Error: cannot delete protected file - " + fileName;
        }

        try {
            Files.delete(file.toPath());
            log.info("[FileDeleteTool] app={} deleted: {}", appId, safe);
            return "OK: deleted " + safe;
        } catch (IOException e) {
            log.error("[FileDeleteTool] app={} failed to delete {}: {}", appId, safe, e.getMessage());
            return "Error: failed to delete " + safe + " - " + e.getMessage();
        }
    }
}
