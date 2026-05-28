package com.notesapp.utils;

import com.notesapp.drivers.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SelfHealingLocator
 *
 * Safe locators that attempt to locate an element using primary and alternative fallbacks to reduce test flakiness.
 */
public class SelfHealingLocator {
    private static final Logger log = LoggerFactory.getLogger(SelfHealingLocator.class);

    public static WebElement findElement(By primary, By... fallbacks) {
        WebDriver driver = DriverManager.getDriver();
        try {
            return driver.findElement(primary);
        } catch (Exception e) {
            log.warn("Primary locator failed: {}. Initiating self-healing...", primary);
            for (By fallback : fallbacks) {
                try {
                    WebElement el = driver.findElement(fallback);
                    log.info("Successfully healed! Found element using fallback: {}", fallback);
                    return el;
                } catch (Exception ignored) {
                }
            }
            log.error("All locators failed for element.");
            throw e;
        }
    }
}
