package com.example.autowebgenerator.core;

import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AiCodeGeneratorFacade — the full end-to-end pipeline.
 *
 * ⚠️  These tests call the REAL OpenAI API — they require a valid api-key in
 *     application-local.yaml (already configured via the local Spring profile).
 */
@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    // -------------------------------------------------------------------------
    // Synchronous tests
    // -------------------------------------------------------------------------

    @Test
    void generateAndSave_htmlMode_createsIndexHtml() {
        File outputDir = aiCodeGeneratorFacade.generateAndSave(
                "A task tracking web app with a form to add tasks and a list to display them",
                CodeGenTypeEnum.HTML
        );

        System.out.println("=== HTML mode output ===");
        System.out.println("Directory: " + outputDir.getAbsolutePath());

        assertNotNull(outputDir);
        assertTrue(outputDir.exists(),      "Output directory should exist");
        assertTrue(outputDir.isDirectory(), "Output path should be a directory");

        File indexHtml = new File(outputDir, "index.html");
        assertTrue(indexHtml.exists(),       "index.html must be created");
        assertTrue(indexHtml.length() > 100, "index.html should contain meaningful content");
    }

    @Test
    void generateAndSave_multiFileMode_createsThreeFiles() {
        File outputDir = aiCodeGeneratorFacade.generateAndSave(
                "A personal portfolio page with sections: About, Skills, and Contact",
                CodeGenTypeEnum.MULTI_FILE
        );

        System.out.println("=== Multi-file mode output ===");
        System.out.println("Directory: " + outputDir.getAbsolutePath());
        for (File f : outputDir.listFiles()) {
            System.out.printf("  %-15s %d bytes%n", f.getName(), f.length());
        }

        assertNotNull(outputDir);
        assertTrue(outputDir.exists());

        File indexHtml = new File(outputDir, "index.html");
        File styleCss  = new File(outputDir, "style.css");
        File scriptJs  = new File(outputDir, "script.js");

        assertTrue(indexHtml.exists() && indexHtml.length() > 0, "index.html must exist and have content");
        assertTrue(styleCss.exists()  && styleCss.length()  > 0, "style.css must exist and have content");
        assertTrue(scriptJs.exists()  && scriptJs.length()  > 0, "script.js must exist and have content");
    }

    // -------------------------------------------------------------------------
    // Streaming tests (Flux)
    // -------------------------------------------------------------------------

    @Test
    void generateAndSaveCodeStream_htmlMode_streamsTokensAndSavesFile() {
        Flux<String> stream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "A simple to-do list app",
                CodeGenTypeEnum.HTML
        );

        // Block until all tokens arrive, collect into a list
        List<String> tokens = stream.collectList().block();

        assertNotNull(tokens,          "Token list should not be null");
        assertFalse(tokens.isEmpty(),  "Should have received at least one token");

        String fullContent = String.join("", tokens);
        assertFalse(fullContent.isBlank(), "Accumulated content should not be blank");

        System.out.println("=== Streaming HTML mode ===");
        System.out.println("Tokens received : " + tokens.size());
        System.out.println("Total length    : " + fullContent.length());
    }

    @Test
    void generateAndSaveCodeStream_multiFileMode_streamsTokensAndSavesFiles() {
        Flux<String> stream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "A task recording website",
                CodeGenTypeEnum.MULTI_FILE
        );

        List<String> tokens = stream.collectList().block();

        assertNotNull(tokens);
        assertFalse(tokens.isEmpty(), "Should have received at least one token");

        String fullContent = String.join("", tokens);
        assertFalse(fullContent.isBlank(), "Accumulated content should not be blank");

        System.out.println("=== Streaming multi-file mode ===");
        System.out.println("Tokens received : " + tokens.size());
        System.out.println("Total length    : " + fullContent.length());
    }
}
