package com.example.autowebgenerator.ai.tools;

import cn.hutool.core.io.FileUtil;
import com.example.autowebgenerator.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * LangChain4j tool for writing project files to disk.
 *
 * The AI calls this tool once per file it wants to create.
 * Files are written under tmp/code_output/vue_project_{appId}/.
 */
@Slf4j
public class FileWriteTool {

    private final long appId;

    public FileWriteTool(long appId) {
        this.appId = appId;
    }

    @Tool("写入文件到项目目录的指定路径")
    public String writeFile(
            @P("相对于项目根目录的文件路径，例如 src/App.vue 或 package.json") String filePath,
            @P("文件的完整内容") String content
    ) {
        try {
            // Sanitise path: strip leading slashes and directory traversal
            String cleanPath = filePath.replaceAll("\\.\\./", "").replaceAll("^/+", "");
            String projectDir = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
            String fullPath = projectDir + "/" + cleanPath;

            FileUtil.writeString(content, fullPath, StandardCharsets.UTF_8);
            log.info("FileWriteTool: wrote {} (app={})", cleanPath, appId);
            return cleanPath;
        } catch (Exception e) {
            log.error("FileWriteTool: failed to write {} (app={})", filePath, appId, e);
            return "Error: " + e.getMessage();
        }
    }
}
