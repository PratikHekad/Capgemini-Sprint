package com.notesapp.stepdefinitions;

import com.notesapp.drivers.DriverManager;
import com.notesapp.utils.ScreenshotUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/**
 * Hooks
 *
 * Cucumber lifecycle hooks. Lazily manages ThreadLocal WebDriver sessions and attaches failure screenshots.
 */
public class Hooks {

    @Before
    public void setUp(Scenario scenario) {
        // DriverManager automatically initializes driver on demand (lazy loading)
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                byte[] screenshot = ScreenshotUtil.captureAsBytes();
                if (screenshot != null) {
                    scenario.attach(screenshot, "image/png", "Failure Screenshot");
                }
            }
        } finally {
            DriverManager.quitDriver();
        }
    }
}
