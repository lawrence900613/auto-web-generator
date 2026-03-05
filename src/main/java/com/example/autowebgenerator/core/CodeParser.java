package com.example.autowebgenerator.core;

import com.example.autowebgenerator.ai.model.HtmlCodeResult;
import com.example.autowebgenerator.ai.model.MultiFileCodeResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts code blocks from the AI's raw markdown streaming response.
 *
 * The streaming endpoint does NOT use JSON mode — the AI returns markdown like:
 *
 *   ```html
 *   <!DOCTYPE html>...
 *   ```
 *
 *   ```css
 *   body { margin: 0; }
 *   ```
 *
 *   ```js
 *   console.log("hello");
 *   ```
 *
 * CodeParser uses regex to pull out each block and populate the result POJOs.
 * This is only needed for streaming; the synchronous endpoint gets structured
 * JSON directly from LangChain4j.
 */
public class CodeParser {

    private static final Pattern HTML_PATTERN =
            Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private static final Pattern CSS_PATTERN =
            Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private static final Pattern JS_PATTERN =
            Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /** Parse the full accumulated stream into an HtmlCodeResult. */
    public static HtmlCodeResult parseHtmlCode(String rawContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        String html = extract(rawContent, HTML_PATTERN);
        // Fallback: if no code fence found, treat the whole response as HTML
        result.setHtmlCode(html != null ? html.trim() : rawContent.trim());
        result.setDescription("Generated via streaming");
        return result;
    }

    /** Parse the full accumulated stream into a MultiFileCodeResult. */
    public static MultiFileCodeResult parseMultiFileCode(String rawContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();

        String html = extract(rawContent, HTML_PATTERN);
        String css  = extract(rawContent, CSS_PATTERN);
        String js   = extract(rawContent, JS_PATTERN);

        if (html != null) result.setHtmlCode(html.trim());
        if (css  != null) result.setCssCode(css.trim());
        if (js   != null) result.setJsCode(js.trim());
        result.setDescription("Generated via streaming");
        return result;
    }

    /**
     * Parse a Vue project streaming response that uses [FILE:path]...[/FILE] delimiters.
     *
     * Returns a path → content map in encounter order.
     * The path is sanitised (no leading slashes, no "../").
     */
    public static Map<String, String> parseVueProjectFiles(String rawContent) {
        Map<String, String> files = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("\\[FILE:([^\\]]+)\\]\\n([\\s\\S]*?)\\n?\\[/FILE\\]");
        Matcher m = pattern.matcher(rawContent);
        while (m.find()) {
            String path = m.group(1).trim().replaceAll("\\.\\./", "").replaceAll("^/+", "");
            String content = m.group(2);
            files.put(path, content);
        }
        return files;
    }

    private static String extract(String content, Pattern pattern) {
        Matcher m = pattern.matcher(content);
        return m.find() ? m.group(1) : null;
    }
}
