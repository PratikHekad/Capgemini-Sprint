package com.notesapp.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * TestRunner — Updated for Section 3.1
 *
 * Main Cucumber runner — executes ALL feature scenarios.
 * Setting parallel=true on the @DataProvider enables concurrent scenario
 * execution when testng-parallel.xml sets thread-count > 1.
 * DriverManager uses ThreadLocal so each thread gets its own browser.
 */
@CucumberOptions(
    features = "src/test/resources/features",
    glue     = "com.notesapp.stepdefinitions",
    plugin   = {
        "pretty",
        "html:target/cucumber-reports.html",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {

    /**
     * parallel = false  → sequential (default testng.xml)
     * parallel = true   → concurrent (testng-parallel.xml)
     *
     * Switch is controlled by the testng.xml that's active:
     * the parallel attribute on the <suite> tag controls thread count.
     */
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
