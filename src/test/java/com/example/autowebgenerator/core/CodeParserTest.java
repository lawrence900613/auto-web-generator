package com.example.autowebgenerator.core;

import com.example.autowebgenerator.ai.model.HtmlCodeResult;
import com.example.autowebgenerator.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CodeParser — no network or Spring context required.
 *
 * These tests simulate the raw markdown text that the AI produces in
 * streaming mode and verify that the regex extraction is correct.
 */
class CodeParserTest {

    // ------------------------------------------------------------------
    // parseHtmlCode
    // ------------------------------------------------------------------

    @Test
    void parseHtmlCode_extractsCodeFromFence() {
        String aiResponse = """
                Here is your website:

                ```html
                <!DOCTYPE html>
                <html><body><h1>Hello World</h1></body></html>
                ```

                I hope this helps!
                """;

        HtmlCodeResult result = CodeParser.parseHtmlCode(aiResponse);

        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        assertTrue(result.getHtmlCode().contains("<!DOCTYPE html>"),
                "HTML code should contain DOCTYPE declaration");
        assertTrue(result.getHtmlCode().contains("Hello World"),
                "HTML code should contain the page content");
    }

    @Test
    void parseHtmlCode_fallsBackToRawContentWhenNoFence() {
        // AI sometimes responds without a code fence
        String rawHtml = "<!DOCTYPE html><html><body>No fence</body></html>";

        HtmlCodeResult result = CodeParser.parseHtmlCode(rawHtml);

        assertNotNull(result);
        assertEquals(rawHtml.trim(), result.getHtmlCode());
    }

    @Test
    void parseHtmlCode_isCaseInsensitiveForFenceLabel() {
        String aiResponse = """
                ```HTML
                <!DOCTYPE html><html></html>
                ```
                """;

        HtmlCodeResult result = CodeParser.parseHtmlCode(aiResponse);
        assertNotNull(result.getHtmlCode());
        assertTrue(result.getHtmlCode().contains("<!DOCTYPE html>"));
    }

    // ------------------------------------------------------------------
    // parseMultiFileCode
    // ------------------------------------------------------------------

    @Test
    void parseMultiFileCode_extractsAllThreeFiles() {
        String aiResponse = """
                Here is the multi-file website:

                ```html
                <!DOCTYPE html>
                <html>
                  <head><link rel="stylesheet" href="style.css"></head>
                  <body><h1>Task Tracker</h1><script src="script.js"></script></body>
                </html>
                ```

                ```css
                body { margin: 0; font-family: Arial, sans-serif; }
                h1   { color: #333; }
                ```

                ```js
                document.addEventListener('DOMContentLoaded', () => {
                  console.log('ready');
                });
                ```
                """;

        MultiFileCodeResult result = CodeParser.parseMultiFileCode(aiResponse);

        assertNotNull(result);
        assertNotNull(result.getHtmlCode(), "htmlCode should not be null");
        assertNotNull(result.getCssCode(),  "cssCode should not be null");
        assertNotNull(result.getJsCode(),   "jsCode should not be null");

        assertTrue(result.getHtmlCode().contains("Task Tracker"));
        assertTrue(result.getCssCode().contains("font-family"));
        assertTrue(result.getJsCode().contains("DOMContentLoaded"));
    }

    @Test
    void parseMultiFileCode_handlesJavascriptFenceLabel() {
        // AI may write ```javascript instead of ```js
        String aiResponse = """
                ```html
                <html></html>
                ```
                ```css
                body{}
                ```
                ```javascript
                console.log('hello');
                ```
                """;

        MultiFileCodeResult result = CodeParser.parseMultiFileCode(aiResponse);

        assertNotNull(result.getJsCode());
        assertTrue(result.getJsCode().contains("console.log"));
    }

    @Test
    void parseMultiFileCode_toleratesMissingCssBlock() {
        // AI might omit CSS if the page needs none
        String aiResponse = """
                ```html
                <html><body>Minimal</body></html>
                ```
                ```js
                // no CSS needed
                ```
                """;

        MultiFileCodeResult result = CodeParser.parseMultiFileCode(aiResponse);

        assertNotNull(result.getHtmlCode());
        assertNull(result.getCssCode(), "cssCode should be null when block is absent");
        assertNotNull(result.getJsCode());
    }
}
