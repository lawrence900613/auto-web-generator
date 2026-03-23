package com.example.autowebgenerator.core.builder;

import com.example.autowebgenerator.constant.AppConstant;
import com.example.autowebgenerator.exception.ErrorCode;
import com.example.autowebgenerator.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Runs `npm install` + `npm run build` for a generated Vue project.
 *
 * The project directory is: tmp/code_output/vue_project_{appId}/
 * After a successful build the dist/ subdirectory contains the deployable site.
 */
@Slf4j
@Component
public class VueProjectBuilder {

    private static final int INSTALL_TIMEOUT_SECONDS = 300;
    private static final int BUILD_TIMEOUT_SECONDS   = 180;

    /** Void HTML elements — closing tags for these are illegal in Vue templates. */
//    private static final List<String> VOID_TAGS = List.of(
//            "area", "base", "br", "col", "embed", "hr", "img", "input",
//            "link", "meta", "param", "source", "track", "wbr"
//    );

    /**
     * Synchronously installs dependencies and builds the Vue project.
     *
     * @param projectPath absolute path to the Vue project root
     * @throws ServiceException if the build fails
     */
    public void buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        File packageJson = new File(projectDir, "package.json");

        if (!packageJson.exists()) {
            throw new ServiceException(ErrorCode.SYSTEM_ERROR,
                    "Cannot build: package.json not found in " + projectPath);
        }

        log.info("VueProjectBuilder: npm install in {}", projectPath);
        runCommand(projectDir, INSTALL_TIMEOUT_SECONDS, npm(), "install");

        log.info("VueProjectBuilder: sanitizing .vue files in {}", projectPath);
        sanitizeVueFiles(projectDir);

        log.info("VueProjectBuilder: npm run build in {}", projectPath);
        runCommand(projectDir, BUILD_TIMEOUT_SECONDS, npm(), "run", "build");

        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            throw new ServiceException(ErrorCode.SYSTEM_ERROR,
                    "Build succeeded but dist/ directory not found in " + projectPath);
        }

        log.info("VueProjectBuilder: build complete for {}", projectPath);
    }

    /**
     * Fires off buildProject in a virtual thread so callers are not blocked.
     * Errors are only logged (not propagated).
     */
    public void buildProjectAsync(String projectPath) {
        Thread thread = new Thread(() -> {
            try {
                buildProject(projectPath);
            } catch (Exception e) {
                log.error("VueProjectBuilder async build failed for {}: {}", projectPath, e.getMessage(), e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ---- sanitizer ----

    /**
     * Walks all .vue files under projectDir and strips closing tags for HTML void elements
     * (e.g. {@code </br>}, {@code </img>}). Vue's template compiler treats these as
     * "Invalid end tag" and aborts the build.
     */
    private void sanitizeVueFiles(File projectDir) {
        try (var stream = Files.walk(projectDir.toPath())) {
            stream.filter(p -> p.toString().endsWith(".vue"))
                  .forEach(this::sanitizeVueFile);
        } catch (Exception e) {
            log.warn("VueProjectBuilder: error walking project for sanitization: {}", e.getMessage());
        }
    }

    private void sanitizeVueFile(Path file) {
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String fixed = content;

            // Replace `function` keyword inside template binding expressions with a stub
            // that produces a clear compile error pointing to the right line.
            // Pattern: :attr="...function(...)" or v-bind:attr="...function(...)"
            // We replace `function(` → `INVALID_FUNCTION_IN_TEMPLATE(` so Vite reports
            // the exact line and the AI auto-fix knows what to look for.
            fixed = fixed.replaceAll(
                    "(?<=[\"'])function\\s*\\(",
                    "INVALID_FUNCTION_IN_TEMPLATE("
            );

            if (!fixed.equals(content)) {
                log.warn("VueProjectBuilder: replaced illegal `function` keyword in template expressions in {}",
                        file.getFileName());
                Files.writeString(file, fixed, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("VueProjectBuilder: failed to sanitize {}: {}", file.getFileName(), e.getMessage());
        }
    }

    // ---- helpers ----

    private void runCommand(File workDir, int timeoutSeconds, String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(workDir);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Drain output so the process doesn't block on a full pipe buffer
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (Thread.currentThread().isInterrupted()) {
                        process.destroyForcibly();
                        throw new InterruptedException("Build interrupted by client disconnect");
                    }
                    log.info("[npm] {}", line);
                    output.append(line).append('\n');
                }
            }

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new ServiceException(ErrorCode.SYSTEM_ERROR,
                        "Command timed out after " + timeoutSeconds + "s: " + String.join(" ", cmd));
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // Include the last 50 lines of output so the AI can understand and fix the error
                String[] lines = output.toString().split("\n");
                int start = Math.max(0, lines.length - 50);
                StringBuilder snippet = new StringBuilder();
                for (int i = start; i < lines.length; i++) {
                    snippet.append(lines[i]).append('\n');
                }
                throw new ServiceException(ErrorCode.SYSTEM_ERROR,
                        "npm build failed:\n" + snippet);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "Build interrupted by client disconnect");
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.SYSTEM_ERROR,
                    "Failed to run command: " + String.join(" ", cmd) + " — " + e.getMessage());
        }
    }

    /** Returns the OS-appropriate npm executable name. */
    private String npm() {
        return System.getProperty("os.name").toLowerCase().contains("win") ? "npm.cmd" : "npm";
    }

    /** Returns the absolute project path for the given appId. */
    public static String projectPath(Long appId) {
        return AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
    }
}
