package com.notesapp.stepdefinitions;

import com.notesapp.config.ConfigReader;
import com.notesapp.drivers.DriverManager;
import com.notesapp.pages.LoginPage;
import org.openqa.selenium.By;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

/**
 * LoginSteps
 *
 * Cucumber step definitions for /login scenarios.
 */
public class LoginSteps {

    private final LoginPage loginPage = new LoginPage();

    @Given("I navigate to the login page")
    public void i_navigate_to_the_login_page() {
        loginPage.openLoginPage();
    }

    @When("I enter email {string} and password {string}")
    public void i_enter_email_and_password(String email, String password) {
        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
    }

    @When("I click the login button")
    public void i_click_the_login_button() {
        loginPage.clickLogin();
    }

    @Then("I should be redirected to the dashboard page")
    public void i_should_be_redirected_to_the_dashboard_page() {
        Assert.assertTrue(loginPage.isDashboardLoaded(), "Dashboard was not loaded successfully!");
    }

    @Then("I should see a login error message")
    public void i_should_see_a_login_error_message() {
        Assert.assertTrue(loginPage.isErrorDisplayed(), "Error message was not displayed!");
        String errMsg = loginPage.getErrorMessage();
        Assert.assertFalse(errMsg.isEmpty(), "Error message text is empty!");
    }

    /**
     * TC-NEG-01 blank fields — browser HTML5 validation prevents form submit,
     * so no server error appears; we simply confirm the user is still on the login page.
     */
    @Then("I should remain on the login page")
    public void i_should_remain_on_the_login_page() {
        Assert.assertTrue(loginPage.isOnLoginPage(),
            "Expected to stay on the login page but current URL is: " + loginPage.getCurrentUrl());
    }

    /**
     * TC-E2E-02 — after an API-side deletion the browser needs a page refresh
     * so the UI reflects the latest server state.
     */
    @When("I refresh the page")
    public void i_refresh_the_page() {
        com.notesapp.drivers.DriverManager.getDriver().navigate().refresh();
        com.notesapp.utils.WaitUtil.waitForPageReady();
    }

    @Given("I login with valid credentials")
    public void i_login_with_valid_credentials() {
        loginPage.login(ConfigReader.getEmail(), ConfigReader.getPassword());
        Assert.assertTrue(loginPage.isDashboardLoaded(), "Dashboard was not loaded successfully during login!");
    }
}
