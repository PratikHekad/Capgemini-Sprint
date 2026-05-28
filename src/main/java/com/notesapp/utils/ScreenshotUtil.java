package com.notesapp.utils;

import com.notesapp.config.ConfigReader;
import com.notesapp.drivers.DriverManager;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ScreenshotUtil
 *
 * Captures screenshots either as raw bytes (for Allure) or to an external file (for ExtentReports).
 */
public class ScreenshotUtil {
    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtil.class);

    public static byte[] captureAsBytes() {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            log.warn("Cannot capture screenshot - WebDriver is null");
            return null;
        }
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.error("Failed to capture screenshot as bytes: {}", e.getMessage());
            return null;
        }
    }

    public static String captureToFile(String testName) {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            log.warn("Cannot capture screenshot - WebDriver is null");
            return "";
        }
        try {
            var srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            var timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            var fileName = testName + "_" + timestamp + ".png";
            var targetDir = ConfigReader.get("screenshot.dir");
            if (targetDir == null) {
                targetDir = "test-output/screenshots";
            }
            var destFile = new File(targetDir + "/" + fileName);
            FileUtils.copyFile(srcFile, destFile);
            log.info("Screenshot saved to: {}", destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            log.error("Failed to save screenshot file: {}", e.getMessage());
            return "";
        }
    }
}
