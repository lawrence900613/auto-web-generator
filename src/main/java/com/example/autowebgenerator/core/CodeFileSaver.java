package com.example.autowebgenerator.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.autowebgenerator.ai.model.HtmlCodeResult;
import com.example.autowebgenerator.ai.model.MultiFileCodeResult;
import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Persists AI-generated code to the local file system.
 *
 * Output directory layout:
 *   {project-root}/tmp/code_output/{mode}_{snowflakeId}/
 *       index.html
 *       style.css   (multi-file mode only)
 *       script.js   (multi-file mode only)
 *
 * A Snowflake ID (via Hutool) guarantees each generation gets a unique folder,
 * so concurrent requests never overwrite each other.
 */
public class CodeFileSaver {

    /** Root directory for all generated output — relative to JVM working directory. */
    static final String OUTPUT_ROOT = System.getProperty("user.dir") + "/tmp/code_output";

    // -------------------------------------------------------------------------
    // Public save methods
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
    // Helpers
    // -------------------------------------------------------------------------

    /** Build a path like: tmp/code_output/html_1234567890 */
    private static String buildUniqueDir(String prefix) {
        String dirName = StrUtil.format("{}_{}", prefix, IdUtil.getSnowflakeNextIdStr());
        String path = OUTPUT_ROOT + File.separator + dirName;
        FileUtil.mkdir(path);
        return path;
    }

    /** Write content to a file, skipping null/blank content. */
    private static void write(String dir, String filename, String content) {
        if (StrUtil.isBlank(content)) return;
        FileUtil.writeString(content, dir + File.separator + filename, StandardCharsets.UTF_8);
    }
}
