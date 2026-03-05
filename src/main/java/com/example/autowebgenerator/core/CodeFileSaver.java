package com.example.autowebgenerator.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.autowebgenerator.ai.model.HtmlCodeResult;
import com.example.autowebgenerator.ai.model.MultiFileCodeResult;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Persists AI-generated code to the local file system.
 *
 * Directory layout:
 *   tmp/code_output/{appId}/          — per-app generated output
 *   tmp/code_output/{mode}_{uuid}/    — legacy anonymous output (no appId)
 *   tmp/code_deploy/{deployKey}/      — publicly served deployed apps
 */
public class CodeFileSaver {

    static final String OUTPUT_ROOT = System.getProperty("user.dir") + "/tmp/code_output";
    static final String DEPLOY_ROOT = System.getProperty("user.dir") + "/tmp/code_deploy";

    // -------------------------------------------------------------------------
    // Save — anonymous (no appId)
    // -------------------------------------------------------------------------

    public static File saveHtmlCode(HtmlCodeResult result) {
        String dir = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        write(dir, "index.html", result.getHtmlCode());
        return new File(dir);
    }

    public static File saveMultiFileCode(MultiFileCodeResult result) {
        String dir = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        write(dir, "index.html", result.getHtmlCode());
        write(dir, "style.css",  result.getCssCode());
        write(dir, "script.js",  result.getJsCode());
        return new File(dir);
    }

    // -------------------------------------------------------------------------
    // Save — per-app (appId as directory name)
    // -------------------------------------------------------------------------

    public static File saveHtmlCodeForApp(HtmlCodeResult result, Long appId) {
        String dir = appOutputDir(appId);
        FileUtil.mkdir(dir);
        write(dir, "index.html", result.getHtmlCode());
        return new File(dir);
    }

    public static File saveMultiFileCodeForApp(MultiFileCodeResult result, Long appId) {
        String dir = appOutputDir(appId);
        FileUtil.mkdir(dir);
        write(dir, "index.html", result.getHtmlCode());
        write(dir, "style.css",  result.getCssCode());
        write(dir, "script.js",  result.getJsCode());
        return new File(dir);
    }

    // -------------------------------------------------------------------------
    // Deploy — copy app output to the deploy directory
    // -------------------------------------------------------------------------

    public static void deployCodeForApp(Long appId, String deployKey) {
        String srcDir = appOutputDir(appId);
        String destDir = DEPLOY_ROOT + File.separator + deployKey;
        FileUtil.mkdir(destDir);
        FileUtil.copyContent(new File(srcDir), new File(destDir), true);
    }

    /**
     * Write all files from a parsed Vue project response to the vue_project_{appId} directory.
     */
    public static void saveVueProjectFiles(Map<String, String> files, Long appId) {
        String projectDir = OUTPUT_ROOT + File.separator + "vue_project_" + appId;
        FileUtil.mkdir(projectDir);
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String relativePath = entry.getKey().replace("/", File.separator);
            String fullPath = projectDir + File.separator + relativePath;
            FileUtil.writeString(entry.getValue(), fullPath, StandardCharsets.UTF_8);
        }
    }

    /**
     * Deploy a built Vue project: copies the dist/ directory to the deploy location.
     * The project must have been built (npm run build) before calling this.
     */
    public static void deployVueProjectForApp(Long appId, String deployKey) {
        String srcDir = OUTPUT_ROOT + File.separator + "vue_project_" + appId + File.separator + "dist";
        String destDir = DEPLOY_ROOT + File.separator + deployKey;
        FileUtil.mkdir(destDir);
        FileUtil.copyContent(new File(srcDir), new File(destDir), true);
    }

    public static String getDeployUrl(String deployKey) {
        return "/deploy/" + deployKey + "/index.html";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String appOutputDir(Long appId) {
        return OUTPUT_ROOT + File.separator + appId;
    }

    private static String buildUniqueDir(String prefix) {
        String dirName = StrUtil.format("{}_{}", prefix, IdUtil.getSnowflakeNextIdStr());
        String path = OUTPUT_ROOT + File.separator + dirName;
        FileUtil.mkdir(path);
        return path;
    }

    private static void write(String dir, String filename, String content) {
        if (StrUtil.isBlank(content)) return;
        FileUtil.writeString(content, dir + File.separator + filename, StandardCharsets.UTF_8);
    }
}
