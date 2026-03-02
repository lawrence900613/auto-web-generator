package com.example.autowebgenerator.core;

import com.example.autowebgenerator.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Files;

/**
 * Quick smoke test — calls the real OpenAI API and prints the generated HTML.
 * Run this to verify the full pipeline works end-to-end.
 */
@SpringBootTest
class HtmlGenerationIT {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateHtml_printOutput() throws Exception {
        File outputDir = aiCodeGeneratorFacade.generateAndSave(
                "A personal finance dashboard with a monthly budget tracker, expense categories (food, rent, transport, entertainment), " +
                "a progress bar for each category showing spending vs budget, a summary card showing total spent and remaining balance, " +
                "and a transaction history table. Use a dark theme with green/red indicators for under/over budget.",
                CodeGenTypeEnum.HTML
        );

        File indexHtml = new File(outputDir, "index.html");
        String html = Files.readString(indexHtml.toPath());

        System.out.println("=== Saved to: " + indexHtml.getAbsolutePath() + " ===");
        System.out.println(html);
    }

    @Test
    void generateMultiFile_printOutput() throws Exception {
        File outputDir = aiCodeGeneratorFacade.generateAndSave(
                "A project management board (Kanban style) with three columns: To Do, In Progress, and Done. " +
                "Each column contains draggable task cards with a title, priority badge (high/medium/low), and a delete button. " +
                "Include a form at the top to add new tasks and assign them to a column. " +
                "Animate card transitions between columns. Use a clean, modern design with a sidebar navigation.",
                CodeGenTypeEnum.MULTI_FILE
        );

        for (String filename : new String[]{"index.html", "style.css", "script.js"}) {
            File file = new File(outputDir, filename);
            System.out.println("=== " + filename + " — " + file.getAbsolutePath() + " ===");
            System.out.println(Files.readString(file.toPath()));
        }
    }
}
