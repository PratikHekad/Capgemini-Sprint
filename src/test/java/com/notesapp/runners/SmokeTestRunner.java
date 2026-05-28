package com.notesapp.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * SmokeTestRunner — Section 3.1
 *
 * Runs only scenarios tagged @smoke — the 3 most critical happy-path checks:
 *   TC-UI-01  (valid login)
 *   TC-API-01 (GET /notes structure)
 *   TC-E2E-03 (API note visible in UI)
 *
 * Used by testng-smoke.xml for fast pre-merge CI feedback (~2-3 min).
 *
 * To add a scenario to smoke: add @smoke tag in the feature file.
 */
@CucumberOptions(
    features   = "src/test/resources/features",
    glue       = "com.notesapp.stepdefinitions",
    tags       = "@smoke",
    plugin     = {
        "pretty",
        "html:target/cucumber-smoke-report.html",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    monochrome = true
)
public class SmokeTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
