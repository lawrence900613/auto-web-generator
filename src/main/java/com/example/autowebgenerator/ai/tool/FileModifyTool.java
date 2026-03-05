package com.example.autowebgenerator.ai.tool;

import cn.hutool.core.io.FileUtil;
import com.example.autowebgenerator.core.builder.VueProjectBuilder;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Targeted file modification tool.
 *
 * Preferred over writeFile when fixing build errors — the AI only needs to
 * replace the broken section rather than regenerating the entire file.
 * This reduces token usage and lowers the risk of introducing new bugs.
 */
@Slf4j
public class FileModifyTool {

    @Tool("Modify a specific section of an existing project file by replacing oldContent with newContent. " +
          "Prefer this over writeFile when fixing targeted errors.")
    public String modifyFile(
            @ToolMemoryId Long appId,
            @P("Relative path of the file to modify, e.g. 'src/App.vue'") String relativePath,
            @P("The exact existing text to find — must match character-for-character") String oldContent,
            @P("The replacement text") String newContent
    ) {
        String projectPath = VueProjectBuilder.projectPath(appId);
        String safe = relativePath.replaceAll("\\.\\./", "").replaceAll("^/+", "");
        File file = new File(projectPath + File.separator + safe.replace("/", File.separator));

        if (!file.exists() || !file.isFile()) {
            return "Error: file not found: " + safe;
        }
        String current = FileUtil.readString(file, StandardCharsets.UTF_8);
        if (!current.contains(oldContent)) {
            return "Error: oldContent not found in " + safe + " — check for exact whitespace/newline match";
        }
        String modified = current.replace(oldContent, newContent);
        FileUtil.writeString(modified, file, StandardCharsets.UTF_8);
        log.info("[FileModifyTool] app={} modified: {}", appId, safe);
        return "OK: modified " + safe;
    }
}
