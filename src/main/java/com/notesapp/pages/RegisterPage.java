package com.notesapp.pages;

import com.notesapp.base.BasePage;
import com.notesapp.config.ConfigReader;
import org.openqa.selenium.By;

/**
 * RegisterPage
 *
 * Covers the /register page.
 * Used in test setup to create accounts programmatically.
 */
public class RegisterPage extends BasePage {

    private final By nameField        = By.id("name");
    private final By emailField       = By.id("email");
    private final By passwordField    = By.id("password");
    private final By confirmPassField = By.id("confirmPassword");
    private final By registerBtn      = By.id("register");
    private final By successMessage   = By.cssSelector(".alert-success, [data-testid='success']");
    private final By errorMessage     = By.cssSelector(".alert-danger, [data-testid='error']");

    public RegisterPage open() {
        navigateTo(ConfigReader.getAppUrl() + "/register");
        return this;
    }

    public RegisterPage enterName(String name)         { type(nameField, name);           return this; }
    public RegisterPage enterEmail(String email)       { type(emailField, email);         return this; }
    public RegisterPage enterPassword(String password) { type(passwordField, password);   return this; }
    public RegisterPage confirmPassword(String pass)   { type(confirmPassField, pass);    return this; }
    public RegisterPage clickRegister()                { click(registerBtn);              return this; }

    /** Full registration in one shot. */
    public void register(String name, String email, String password) {
        log.info("Registering account: {}", email);
        open();
        enterName(name);
        enterEmail(email);
        enterPassword(password);
        confirmPassword(password);
        clickRegister();
    }

    public boolean isRegistrationSuccessful() { return isVisible(successMessage); }
    public String  getErrorMessage()          { return isVisible(errorMessage) ? getText(errorMessage) : ""; }
}
