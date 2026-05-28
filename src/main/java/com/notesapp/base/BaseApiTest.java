package com.notesapp.base;

import com.notesapp.api.ApiConfig;
import org.testng.annotations.BeforeSuite;

/**
 * BaseApiTest
 *
 * Parent for all pure API test classes.
 * needsBrowser() = false so no Chrome window is opened.
 * RestAssured is initialized once for the whole suite.
 */
public class BaseApiTest extends BaseTest {

    @Override
    protected boolean needsBrowser() {
        return false;
    }

    @BeforeSuite(alwaysRun = true)
    public void initRestAssured() {
        ApiConfig.init();
    }
}
