package com.notesapp.drivers;

import com.notesapp.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DriverManager
 *
 * ThreadLocal WebDriver — each parallel test thread gets its own browser.
 * Always call quitDriver() in @AfterMethod or the browser will stay open.
 */
public class DriverManager {

    private static final Logger log = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverHolder = new ThreadLocal<>();

    /** Returns the driver for this thread. Creates one if it doesn't exist yet. */
    public static WebDriver getDriver() {
        if (driverHolder.get() == null) {
            initDriver();
        }
        return driverHolder.get();
    }

    /** Creates a new browser session for the current thread. */
    public static void initDriver() {
        String  browser  = ConfigReader.getBrowser().toLowerCase().trim();
        boolean headless = ConfigReader.isHeadless();

        WebDriver driver = switch (browser) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ffOpts = new FirefoxOptions();
                if (headless) ffOpts.addArguments("--headless");
                log.info("Firefox started (headless={})", headless);
                yield new FirefoxDriver(ffOpts);
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                if (headless) {
                    opts.addArguments("--headless=new");
                    opts.addArguments("--no-sandbox");
                    opts.addArguments("--disable-dev-shm-usage");
                }
                opts.addArguments("--window-size=1920,1080");
                opts.addArguments("--disable-notifications");
                log.info("Chrome started (headless={})", headless);
                yield new ChromeDriver(opts);
            }
        };

        driver.manage().window().maximize();
        driverHolder.set(driver);
    }

    /** Closes the browser and clears the ThreadLocal reference. */
    public static void quitDriver() {
        WebDriver driver = driverHolder.get();
        if (driver != null) {
            driver.quit();
            driverHolder.remove();
            log.info("Driver closed — thread: {}", Thread.currentThread().getName());
        }
    }
}
