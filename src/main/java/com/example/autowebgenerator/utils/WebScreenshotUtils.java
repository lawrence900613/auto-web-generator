package com.example.autowebgenerator.utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;

@Slf4j
@Component
public class WebScreenshotUtils {

    private static final int WIDTH  = 1600;
    private static final int HEIGHT = 900;

    private boolean available = false;

    @PostConstruct
    public void init() {
        try {
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
            available = true;
            log.info("WebScreenshotUtils: ChromeDriver setup complete");
        } catch (Exception e) {
            log.warn("WebScreenshotUtils: ChromeDriver not available — cover screenshots disabled. Reason: {}", e.getMessage());
        }
    }

    /**
     * Opens the given URL in a headless Chrome browser, waits for full page load,
     * takes a screenshot, compresses it to JPEG, and saves it to savePath.
     *
     * @return savePath on success, null on failure
     */
    public String saveWebPageScreenshot(String webUrl, String savePath) {
        if (!available) {
            log.warn("Chrome unavailable, skipping screenshot for {}", webUrl);
            return null;
        }
        WebDriver driver = null;
        try {
            driver = createDriver();
            driver.get(webUrl);
            waitForPageLoad(driver);

            byte[] raw = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            compressAndSave(raw, savePath);
            log.info("Cover screenshot saved to {}", savePath);
            return savePath;
        } catch (Exception e) {
            log.error("Screenshot failed for {}: {}", webUrl, e.getMessage());
            return null;
        } finally {
            if (driver != null) {
                try { driver.quit(); } catch (Exception ignored) {}
            }
        }
    }

    private WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments(String.format("--window-size=%d,%d", WIDTH, HEIGHT));
        options.addArguments("--disable-extensions");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return driver;
    }

    private void waitForPageLoad(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> ((JavascriptExecutor) d)
                            .executeScript("return document.readyState").equals("complete"));
            // Extra wait for JS-rendered content (Vue, React, etc.)
            Thread.sleep(2000);
        } catch (Exception e) {
            log.warn("waitForPageLoad: {}", e.getMessage());
        }
    }

    private void compressAndSave(byte[] pngBytes, String savePath) throws Exception {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(pngBytes));

        // Scale to 1200×675 thumbnail (16:9)
        int w = 1200, h = 675;
        BufferedImage thumb = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = thumb.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, w, h, null);
        g2.dispose();

        // Write as JPEG at 70% quality
        File out = new File(savePath);
        out.getParentFile().mkdirs();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.7f);
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(thumb, null, null), param);
        }
        writer.dispose();
    }

    @PreDestroy
    public void destroy() {
        // Drivers are created and quit per-call; nothing to clean up here
    }
}
