package com.example.autowebgenerator.service;

import com.example.autowebgenerator.mapper.AppMapper;
import com.example.autowebgenerator.model.entity.App;
import com.example.autowebgenerator.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppCoverService {

    @Resource
    private WebScreenshotUtils webScreenshotUtils;

    @Resource
    private AppMapper appMapper;

    @Value("${server.port:8123}")
    private int serverPort;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    /**
     * Asynchronously takes a screenshot of the deployed app and saves it as the app cover.
     * Called after a successful deploy. Silently skips if Chrome is unavailable.
     */
    @Async
    public void generateCoverAsync(Long appId, String deployKey) {
        log.info("Generating cover for app {} (deployKey={})", appId, deployKey);
        try {
            String deployUrl = "http://localhost:" + serverPort + contextPath
                    + "/deploy/" + deployKey + "/index.html";

            String coversDir = System.getProperty("user.dir") + "/tmp/covers/";
            String filePath  = coversDir + appId + ".jpg";

            String saved = webScreenshotUtils.saveWebPageScreenshot(deployUrl, filePath);
            if (saved == null) {
                log.warn("Cover screenshot skipped for app {}", appId);
                return;
            }

            // Store the URL path that the frontend can use to load the cover
            String coverUrl = contextPath + "/covers/" + appId + ".jpg";
            App update = new App();
            update.setId(appId);
            update.setCover(coverUrl);
            appMapper.update(update);

            log.info("Cover updated for app {}: {}", appId, coverUrl);
        } catch (Exception e) {
            log.error("Cover generation failed for app {}: {}", appId, e.getMessage());
        }
    }
}
