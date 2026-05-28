package com.notesapp.pages;

import com.notesapp.base.BasePage;
import com.notesapp.config.ConfigReader;
import org.openqa.selenium.By;

/**
 * LoginPage
 *
 * Covers the /login page.
 * Locators use element IDs first (most stable), CSS selectors as fallback.
 */
public class LoginPage extends BasePage {

    // ── Locators ─────────────────────────────────────────────────────────────
    private final By emailField     = By.id("email");
    private final By passwordField  = By.id("password");
    private final By loginButton    = By.cssSelector("[data-testid='login-submit']");
    private final By errorMessage   = By.cssSelector(".alert-danger, [data-testid='alert-message']");
    private final By dashboardCheck = By.xpath("//a[contains(text(),'Logout')] | //button[contains(text(),'Logout')] | //*[@data-testid='logout'] | //*[@data-testid='add-note-btn']");
    private final By navLoginLink   = By.xpath("//a[contains(@href,'login') or contains(text(),'Login')]");

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Navigates directly to the login page URL. */
    public LoginPage openLoginPage() {
        navigateTo(ConfigReader.getAppUrl() + "/login");
        log.info("Opened login page");
        return this;
    }

    public LoginPage enterEmail(String email) {
        type(emailField, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordField, password);
        return this;
    }

    public LoginPage clickLogin() {
        click(loginButton);
        log.info("Clicked Login button");
        return this;
    }

    /**
     * Full login in one call.
     * Opens the page, types both fields, clicks submit.
     */
    public LoginPage login(String email, String password) {
        openLoginPage();
        enterEmail(email);
        enterPassword(password);
        clickLogin();
        log.info("Login submitted for: {}", email);
        return this;
    }

    // ── State checks ──────────────────────────────────────────────────────────

    /**
     * Returns true if a dashboard-specific element is visible.
     * More reliable than checking the URL alone.
     */
    public boolean isDashboardLoaded() {
        try {
            waitForVisible(dashboardCheck);
            return true;
        } catch (Exception e) {
            log.error("Dashboard check failed. Current URL: {}", getCurrentUrl());
            if (isVisible(errorMessage)) {
                log.error("Error message displayed on login page: {}", getErrorMessage());
            } else {
                try {
                    String bodyText = driver.findElement(By.tagName("body")).getText();
                    log.error("Does body contain 'Logout'? {}", bodyText.contains("Logout"));
                    log.error("Does body contain 'Add Note'? {}", bodyText.contains("Add Note"));
                    log.error("Does body contain 'Login'? {}", bodyText.contains("Login"));
                    log.error("Does body contain 'Register'? {}", bodyText.contains("Register"));
                    log.error("Entire body text:\n{}", bodyText);
                } catch (Exception ignored) {}
            }
            return false;
        }
    }

    public boolean isErrorDisplayed() {
        try {
            waitForVisible(errorMessage);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        return isVisible(errorMessage) ? getText(errorMessage) : "";
    }

    public boolean isOnLoginPage() {
        return getCurrentUrl().contains("/login");
    }
}
