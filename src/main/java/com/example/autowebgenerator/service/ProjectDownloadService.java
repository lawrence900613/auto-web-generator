package com.example.autowebgenerator.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Slf4j
@Service
public class ProjectDownloadService {

    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules", ".git", "dist", "build", ".DS_Store",
            ".env", "target", ".mvn", ".idea", ".vscode"
    );

    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log", ".tmp", ".cache"
    );

    /**
     * Streams the project directory as a ZIP file into the HTTP response.
     *
     * @param projectPath      absolute path to the project source directory
     * @param downloadFileName filename for the ZIP (e.g. "123456.zip")
     * @param response         HttpServletResponse to write into
     */
    public void downloadProjectAsZip(String projectPath, String downloadFileName,
                                     HttpServletResponse response) throws IOException {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project source not found");
            return;
        }

        Path projectRoot = projectDir.toPath();

        FileFilter filter = file -> {
            Path filePath = file.toPath();
            // Check every path component relative to project root
            Path relative = projectRoot.relativize(filePath);
            for (int i = 0; i < relative.getNameCount(); i++) {
                String component = relative.getName(i).toString();
                if (IGNORED_NAMES.contains(component)) {
                    return false;
                }
                // Check extension only for files (not directories)
                if (file.isFile()) {
                    int dot = component.lastIndexOf('.');
                    if (dot >= 0) {
                        String ext = component.substring(dot).toLowerCase();
                        if (IGNORED_EXTENSIONS.contains(ext)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        };

        String encodedName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFileName
                + "\"; filename*=UTF-8''" + encodedName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        try {
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("Failed to zip project {}: {}", projectPath, e.getMessage());
            throw new IOException("Failed to create ZIP", e);
        }
    }
}
