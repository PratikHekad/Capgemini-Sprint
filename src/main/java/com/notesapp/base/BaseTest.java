package com.notesapp.base;

import com.notesapp.drivers.DriverManager;
import com.notesapp.utils.ScreenshotUtil;
import io.qameta.allure.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * BaseTest
 *
 * Parent for all UI test classes.
 * Handles:
 *   - Opening a browser before each test (@BeforeMethod)
 *   - Capturing a screenshot on failure and attaching it to Allure
 *   - Closing the browser after each test (@AfterMethod)
 *
 * API-only tests extend BaseApiTest which overrides needsBrowser() = false.
 */
public class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Override in BaseApiTest to skip WebDriver setup for API tests. */
    protected boolean needsBrowser() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        if (needsBrowser()) {
            DriverManager.initDriver();
            log.info("Browser opened — thread: {}", Thread.currentThread().getName());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE && needsBrowser()) {
            log.warn("FAILED: {} — capturing screenshot", result.getName());
            byte[] shot = ScreenshotUtil.captureAsBytes();
            if (shot != null) attachFailureScreenshot(shot);
        }

        if (needsBrowser()) {
            DriverManager.quitDriver();
            log.info("Browser closed — test: {}", result.getName());
        }
    }

    @Attachment(value = "Failure Screenshot", type = "image/png")
    private byte[] attachFailureScreenshot(byte[] screenshot) {
        return screenshot;
    }

    protected org.openqa.selenium.WebDriver getDriver() {
        return DriverManager.getDriver();
    }
}
