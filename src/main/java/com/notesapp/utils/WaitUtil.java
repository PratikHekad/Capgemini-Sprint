package com.notesapp.utils;

import com.notesapp.drivers.DriverManager;
import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WaitUtil
 *
 * Page load and thread sleep helper utilities.
 */
public class WaitUtil {
    private static final Logger log = LoggerFactory.getLogger(WaitUtil.class);

    public static void waitForPageReady() {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return;
        }
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").toString().equals("complete"));
            log.debug("Page is fully loaded");
        } catch (Exception e) {
            log.warn("Timeout waiting for page to load: {}", e.getMessage());
        }
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
