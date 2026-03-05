package com.example.autowebgenerator.ai.tool;

import cn.hutool.core.io.FileUtil;
import com.example.autowebgenerator.core.builder.VueProjectBuilder;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

/**
 * Lists the files currently written in the project directory.
 *
 * Useful during auto-fix: the AI can check which files exist before
 * deciding whether to modify or rewrite them.
 */
@Slf4j
public class FileDirReadTool {

    private static final List<String> SKIP_DIRS = List.of("node_modules", ".git", "dist", "build");

    @Tool("List all files currently in the project directory. Use '.' for the project root.")
    public String readDir(
            @ToolMemoryId Long appId,
            @P("Relative directory path to list, e.g. 'src' or '.' for project root") String dir
    ) {
        String projectPath = VueProjectBuilder.projectPath(appId);
        String safe = dir.replaceAll("\\.\\./", "").replaceAll("^/+", "");
        File target = (safe.isEmpty() || ".".equals(safe))
                ? new File(projectPath)
                : new File(projectPath + File.separator + safe.replace("/", File.separator));

        if (!target.exists() || !target.isDirectory()) {
            return "Error: directory not found: " + dir;
        }

        List<File> files = FileUtil.loopFiles(target, f -> {
            String abs = f.getAbsolutePath();
            return SKIP_DIRS.stream().noneMatch(skip ->
                    abs.contains(File.separator + skip + File.separator) ||
                    abs.endsWith(File.separator + skip));
        });

        if (files.isEmpty()) return "(empty)";

        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            String rel = f.getAbsolutePath()
                    .substring(projectPath.length() + 1)
                    .replace(File.separator, "/");
            sb.append(rel).append('\n');
        }
        return sb.toString().trim();
    }
}
