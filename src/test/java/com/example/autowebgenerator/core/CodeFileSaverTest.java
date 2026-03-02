package com.example.autowebgenerator.core;

import com.example.autowebgenerator.ai.model.HtmlCodeResult;
import com.example.autowebgenerator.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CodeFileSaver — writes to the real file system under tmp/.
 *
 * Each test verifies that the correct files are created with non-empty content.
 * The generated directories are NOT cleaned up so you can inspect the output.
 */
class CodeFileSaverTest {

    @Test
    void saveHtmlCode_createsDirectoryWithIndexHtml() {
        HtmlCodeResult result = new HtmlCodeResult();
        result.setHtmlCode("<!DOCTYPE html><html><body><h1>Test</h1></body></html>");
        result.setDescription("Test page");

        File dir = CodeFileSaver.saveHtmlCode(result);

        assertNotNull(dir, "Saved directory should not be null");
        assertTrue(dir.exists(), "Directory should exist on disk");
        assertTrue(dir.isDirectory(), "Path should be a directory");

        File indexHtml = new File(dir, "index.html");
        assertTrue(indexHtml.exists(), "index.html should be created");
        assertTrue(indexHtml.length() > 0, "index.html should not be empty");

        System.out.println("HTML output saved to: " + dir.getAbsolutePath());
    }

    @Test
    void saveMultiFileCode_createsThreeFiles() {
        MultiFileCodeResult result = new MultiFileCodeResult();
        result.setHtmlCode("<!DOCTYPE html><html><head><link rel='stylesheet' href='style.css'></head><body></body></html>");
        result.setCssCode("body { margin: 0; }");
        result.setJsCode("console.log('hello');");
        result.setDescription("Multi-file test");

        File dir = CodeFileSaver.saveMultiFileCode(result);

        assertTrue(dir.exists());

        File indexHtml = new File(dir, "index.html");
        File styleCss  = new File(dir, "style.css");
        File scriptJs  = new File(dir, "script.js");

        assertTrue(indexHtml.exists(), "index.html must be created");
        assertTrue(styleCss.exists(),  "style.css must be created");
        assertTrue(scriptJs.exists(),  "script.js must be created");

        assertTrue(indexHtml.length() > 0);
        assertTrue(styleCss.length()  > 0);
        assertTrue(scriptJs.length()  > 0);

        System.out.println("Multi-file output saved to: " + dir.getAbsolutePath());
    }

    @Test
    void saveHtmlCode_eachCallCreatesUniqueDirectory() {
        HtmlCodeResult result = new HtmlCodeResult();
        result.setHtmlCode("<html></html>");

        File dir1 = CodeFileSaver.saveHtmlCode(result);
        File dir2 = CodeFileSaver.saveHtmlCode(result);

        assertNotEquals(dir1.getAbsolutePath(), dir2.getAbsolutePath(),
                "Consecutive saves must produce different directories (Snowflake ID)");
    }

    @Test
    void saveMultiFileCode_skipsNullContent() {
        // CSS is intentionally null — saver should not crash and should not create the file
        MultiFileCodeResult result = new MultiFileCodeResult();
        result.setHtmlCode("<html></html>");
        result.setCssCode(null);
        result.setJsCode("console.log('no css');");

        File dir = CodeFileSaver.saveMultiFileCode(result);

        assertTrue(new File(dir, "index.html").exists());
        assertFalse(new File(dir, "style.css").exists(),
                "style.css should NOT be created when cssCode is null");
        assertTrue(new File(dir, "script.js").exists());
    }
}
