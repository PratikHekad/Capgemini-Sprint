package com.notesapp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer
 *
 * Automatically retries failed tests up to a maximum limit (2 retries).
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(RetryAnalyzer.class);
    private int count = 0;
    private static final int MAX_LIMIT = 2;

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && this.count < MAX_LIMIT) {
            this.count++;
            log.warn("Retrying failed test: {} (Attempt {}/{})", result.getName(), this.count, MAX_LIMIT);
            return true;
        }
        return false;
    }
}
