package com.example.autowebgenerator.ai;

import com.example.autowebgenerator.ai.model.HtmlCodeResult;
import com.example.autowebgenerator.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AiCodeGeneratorService.
 *
 * ⚠️  These tests call the REAL OpenAI API — they require a valid api-key in
 *     application-local.yaml (already configured via the local Spring profile).
 *
 * What is being tested:
 *   - The LangChain4j proxy is correctly created and injected by Spring
 *   - Structured JSON output is successfully parsed into HtmlCodeResult / MultiFileCodeResult
 *   - The AI response is non-null and contains actual code
 *
 * These mirror the tests shown in the tutorial (Section 4.2).
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode_returnsNonNullResultWithCode() {
        // Prompt kept simple to minimise token usage and latency in tests
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(
                "A minimal personal blog homepage with a header, a list of 3 recent posts, and a footer"
        );

        System.out.println("=== generateHtmlCode result ===");
        System.out.println("Description : " + result.getDescription());
        System.out.println("HTML length : " + (result.getHtmlCode() != null ? result.getHtmlCode().length() : "null"));

        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getHtmlCode(), "htmlCode should not be null");
        assertFalse(result.getHtmlCode().isBlank(), "htmlCode should not be blank");
        assertTrue(result.getHtmlCode().toLowerCase().contains("<!doctype html>") ||
                   result.getHtmlCode().toLowerCase().contains("<html"),
                "htmlCode should contain an HTML tag");
    }

    @Test
    void generateMultiFileCode_returnsAllThreeFiles() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(
                "A simple to-do list app with add, complete, and delete functionality"
        );

        System.out.println("=== generateMultiFileCode result ===");
        System.out.println("Description : " + result.getDescription());
        System.out.println("HTML length : " + (result.getHtmlCode() != null ? result.getHtmlCode().length() : "null"));
        System.out.println("CSS  length : " + (result.getCssCode()  != null ? result.getCssCode().length()  : "null"));
        System.out.println("JS   length : " + (result.getJsCode()   != null ? result.getJsCode().length()   : "null"));

        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getHtmlCode(), "htmlCode should not be null");
        assertNotNull(result.getCssCode(),  "cssCode should not be null");
        assertNotNull(result.getJsCode(),   "jsCode should not be null");

        assertFalse(result.getHtmlCode().isBlank(), "htmlCode should not be blank");
        assertFalse(result.getCssCode().isBlank(),  "cssCode should not be blank");
        assertFalse(result.getJsCode().isBlank(),   "jsCode should not be blank");
    }
}
