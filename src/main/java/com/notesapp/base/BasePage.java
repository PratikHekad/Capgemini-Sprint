package com.notesapp.base;

import com.notesapp.config.ConfigReader;
import com.notesapp.drivers.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * BasePage
 *
 * Parent for all Page Objects.
 * All Selenium interaction lives here — page classes focus only on their
 * own locators and business actions.
 *
 * No PageFactory. Plain By locators are easier to read and debug.
 */
public class BasePage {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage() {
        this.driver = DriverManager.getDriver();
        int timeout = ConfigReader.getExplicitWait();
        if (timeout <= 0) timeout = 15;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    public void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        driver.get(url);
        hideCookieConsent();
    }

    protected void hideCookieConsent() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "var removeBySelector = function(sel) {" +
                "  try {" +
                "    document.querySelectorAll(sel).forEach(function(el) { el.remove(); });" +
                "  } catch(e) {}" +
                "};" +
                "removeBySelector('.fc-consent-root');" +
                "removeBySelector('.fc-dialog-container');" +
                "removeBySelector('.cookie-consent');" +
                "removeBySelector('.consent-banner');" +
                "removeBySelector('#cookie-consent-banner');" +
                "removeBySelector('[class*=\"consent\"]');" +
                "removeBySelector('[id*=\"consent\"]');" +
                "removeBySelector('[class*=\"cookie\"]');" +
                "removeBySelector('[id*=\"cookie\"]');" +
                "removeBySelector('.fc-ab-root');" +
                "removeBySelector('.google-consent');" +
                "removeBySelector('.modal-backdrop.fade:not(.show)');" +
                "removeBySelector('[data-google-vignette]');" +
                "removeBySelector('[class*=\"google-vignette\"]');" +
                "removeBySelector('ins.adsbygoogle');" +
                "removeBySelector('[id*=\"google_ads\"]');" +
                "document.querySelectorAll('iframe').forEach(function(iframe) {" +
                "  try {" +
                "    var src = iframe.src || '';" +
                "    var id = iframe.id || '';" +
                "    var cls = iframe.className || '';" +
                "    if (src.indexOf('google') !== -1 || src.indexOf('consent') !== -1 || src.indexOf('fundingchoices') !== -1 ||" +
                "        id.indexOf('google') !== -1 || id.indexOf('consent') !== -1 ||" +
                "        cls.indexOf('google') !== -1 || cls.indexOf('consent') !== -1) {" +
                "      iframe.remove();" +
                "    }" +
                "  } catch(e) {}" +
                "});" +
                "try {" +
                "  document.body.style.overflow = 'auto';" +
                "  document.documentElement.style.overflow = 'auto';" +
                "  document.body.classList.remove('google-vignette-active', 'modal-open');" +
                "  document.documentElement.classList.remove('google-vignette-active', 'modal-open');" +
                "} catch(e) {}" +
                "if (!window.animationsDisabled) {" +
                "  var style = document.createElement('style');" +
                "  style.type = 'text/css';" +
                "  style.innerHTML = '* { transition: none !important; animation: none !important; transition-duration: 0s !important; animation-duration: 0s !important; }';" +
                "  document.head.appendChild(style);" +
                "  window.animationsDisabled = true;" +
                "}"
            );
            log.debug("Consent dialogs hidden and animations disabled");
        } catch (Exception ignored) {}
    }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }
    public String getPageTitle()  { return driver.getTitle(); }

    // ── Waits ─────────────────────────────────────────────────────────────────

    protected WebElement waitForVisible(By locator) {
        hideCookieConsent();
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        hideCookieConsent();
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected boolean waitForInvisible(By locator) {
        hideCookieConsent();
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected List<WebElement> waitForAllVisible(By locator) {
        hideCookieConsent();
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    protected void click(By locator) {
        try {
            waitForClickable(locator).click();
            log.debug("Clicked: {}", locator);
        } catch (Exception e) {
            try {
                scrollIntoView(locator);
                waitForClickable(locator).click();
                log.debug("Clicked after scroll: {}", locator);
            } catch (ElementClickInterceptedException ex) {
                log.warn("Click intercepted for {}, falling back to JS Click. Error: {}", locator, ex.getMessage());
                jsClick(locator);
            }
        }
    }

    protected void click(WebElement element) {
        try {
            hideCookieConsent();
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
            log.debug("Clicked WebElement successfully");
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
                wait.until(ExpectedConditions.elementToBeClickable(element)).click();
                log.debug("Clicked WebElement after scroll");
            } catch (Exception ex) {
                log.warn("Click intercepted for element, falling back to JS Click. Error: {}", ex.getMessage());
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
        }
    }

    protected void type(By locator, String text) {
        WebElement el = waitForVisible(locator);
        try {
            el.click();
        } catch (Exception ignored) {}
        el.clear();
        el.sendKeys(text);
        try {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];" +
                "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                el, text
            );
        } catch (Exception ignored) {}
        log.debug("Typed '{}' into {}", text, locator);
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }

    protected void selectByText(By locator, String text) {
        WebElement el = waitForVisible(locator);
        new Select(el).selectByVisibleText(text);
        log.debug("Selected '{}' from {}", text, locator);
    }

    // ── Visibility ────────────────────────────────────────────────────────────

    protected boolean isVisible(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    // ── JavaScript ────────────────────────────────────────────────────────────

    protected void jsClick(By locator) {
        WebElement el = waitForVisible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        log.debug("JS-clicked: {}", locator);
    }

    protected void scrollIntoView(By locator) {
        WebElement el = waitForVisible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
    }

    /** Returns page load time in ms using the Navigation Timing API. */
    public long getPageLoadTimeMs() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long start = (Long) js.executeScript("return window.performance.timing.navigationStart;");
        long end   = (Long) js.executeScript("return window.performance.timing.loadEventEnd;");
        return end - start;
    }
}
