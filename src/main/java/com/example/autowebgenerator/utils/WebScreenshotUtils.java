package com.example.autowebgenerator.utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
public class WebScreenshotUtils {

    private static final int WIDTH = 1600;
    private static final int HEIGHT = 900;

    private boolean available = false;
    private String chromeBinary;
    private String chromeDriverBinary;

    @PostConstruct
    public void init() {
        try {
            chromeBinary = detectChromeBinary();
            chromeDriverBinary = detectChromeDriverBinary();

            if (chromeBinary != null && chromeDriverBinary != null) {
                System.setProperty("webdriver.chrome.driver", chromeDriverBinary);
                available = true;
                log.info("WebScreenshotUtils: using local chrome={} chromedriver={}", chromeBinary, chromeDriverBinary);
                return;
            }

            // Fallback for local non-Docker environments.
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
            chromeDriverBinary = System.getProperty("webdriver.chrome.driver");
            available = true;
            log.info("WebScreenshotUtils: ChromeDriver setup complete via WebDriverManager");
        } catch (Exception e) {
            log.warn("WebScreenshotUtils: Chrome/Driver not available, screenshots disabled. Reason: {}", e.getMessage());
        }
    }

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
                try {
                    driver.quit();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-background-networking");
        options.addArguments("--window-size=" + WIDTH + "," + HEIGHT);

        ChromeDriverService service;
        if (chromeDriverBinary != null && !chromeDriverBinary.isBlank()) {
            service = new ChromeDriverService.Builder()
                    .usingDriverExecutable(new File(chromeDriverBinary))
                    .build();
        } else {
            service = new ChromeDriverService.Builder().build();
        }

        WebDriver driver = new ChromeDriver(service, options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        return driver;
    }

    private void waitForPageLoad(WebDriver driver) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
            Thread.sleep(2000);
        } catch (Exception e) {
            log.warn("waitForPageLoad: {}", e.getMessage());
        }
    }

    private void compressAndSave(byte[] pngBytes, String savePath) throws Exception {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(pngBytes));

        int w = 1200;
        int h = 675;
        BufferedImage thumb = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = thumb.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, w, h, null);
        g2.dispose();

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

    private String detectChromeBinary() {
        String fromEnv = System.getenv("CHROME_BIN");
        if (isExisting(fromEnv)) return fromEnv;

        String[] candidates = {
                "/usr/bin/chromium",
                "/usr/bin/chromium-browser",
                "/usr/bin/google-chrome"
        };
        for (String c : candidates) {
            if (isExisting(c)) return c;
        }
        return null;
    }

    private String detectChromeDriverBinary() {
        String fromEnv = System.getenv("CHROMEDRIVER_BIN");
        if (isExisting(fromEnv)) return fromEnv;

        String[] candidates = {
                "/usr/bin/chromedriver",
                "/usr/lib/chromium/chromedriver"
        };
        for (String c : candidates) {
            if (isExisting(c)) return c;
        }
        return null;
    }

    private boolean isExisting(String path) {
        return path != null && !path.isBlank() && new File(path).exists();
    }

    @PreDestroy
    public void destroy() {
        // drivers are per-call
    }
}
