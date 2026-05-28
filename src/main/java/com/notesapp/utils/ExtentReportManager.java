package com.notesapp.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.notesapp.config.ConfigReader;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * ExtentReportManager
 *
 * TestNG listener that creates a beautiful dark-themed HTML test execution report.
 */
public class ExtentReportManager implements ITestListener {
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testHolder = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        String reportDir = ConfigReader.get("extent.report.dir");
        if (reportDir == null) {
            reportDir = "test-output/reports";
        }
        ExtentSparkReporter spark = new ExtentSparkReporter(reportDir + "/ExtentReport.html");
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("Notes App Automation Test Report");
        spark.config().setReportName("Capstone T7B - Test Execution Report");
        
        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        extent.setSystemInfo("Browser", ConfigReader.getBrowser());
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
        testHolder.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (testHolder.get() != null) {
            testHolder.get().log(Status.PASS, "Test Passed");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (testHolder.get() != null) {
            testHolder.get().log(Status.FAIL, "Test Failed: " + result.getThrowable());
            String screenshotPath = ScreenshotUtil.captureToFile(result.getName());
            if (!screenshotPath.isEmpty()) {
                testHolder.get().addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (testHolder.get() != null) {
            testHolder.get().log(Status.SKIP, "Test Skipped: " + result.getThrowable());
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }

    /** Helper to log custom messages into Cucumber / TestNG Extent execution threads. */
    public static ExtentTest getTest() {
        return testHolder.get();
    }
}
