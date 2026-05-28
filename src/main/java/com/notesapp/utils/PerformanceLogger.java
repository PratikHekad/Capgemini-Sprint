package com.notesapp.utils;

import com.notesapp.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PerformanceLogger — Section 3.5
 *
 * Two jobs:
 *  1. Assert that a measured time is under the configured threshold.
 *  2. Append a row to a CSV trend file so performance is tracked across runs.
 *
 * CSV location: test-output/performance/perf-trend.csv
 * Each row: timestamp, test_name, operation, duration_ms, threshold_ms, status
 */
public class PerformanceLogger {

    private static final Logger log = LoggerFactory.getLogger(PerformanceLogger.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private PerformanceLogger() {}

    /**
     * Check that the measured time is under the threshold.
     * Writes the result to the trend CSV regardless of pass/fail.
     *
     * @param testName    name of the test or operation (for the CSV)
     * @param operation   e.g. "GET /notes", "UI Login Page Load"
     * @param durationMs  the actual measured time
     * @param thresholdMs the max allowed time
     * @return true if under threshold, false if over
     */
    public static boolean assertUnderThreshold(String testName, String operation,
                                               long durationMs, long thresholdMs) {
        boolean passed = durationMs < thresholdMs;
        String  status = passed ? "PASS" : "FAIL";

        if (passed) {
            log.info("PERF [{}] {} → {}ms (limit: {}ms) ✓ PASS", testName, operation, durationMs, thresholdMs);
        } else {
            log.warn("PERF [{}] {} → {}ms EXCEEDED limit of {}ms ✗ FAIL", testName, operation, durationMs, thresholdMs);
        }

        writeToCsv(testName, operation, durationMs, thresholdMs, status);
        return passed;
    }

    /**
     * Convenience: reads threshold from config.properties by key.
     * Key format: perf.api.get.notes.ms, perf.api.post.note.ms, etc.
     */
    public static boolean assertApiTime(String testName, String configKey, long durationMs) {
        long threshold = ConfigReader.getInt(configKey);
        if (threshold <= 0) threshold = 2000;
        return assertUnderThreshold(testName, configKey, durationMs, threshold);
    }

    /**
     * Convenience: reads UI threshold from config.properties by key.
     */
    public static boolean assertUiTime(String testName, String configKey, long durationMs) {
        long threshold = ConfigReader.getInt(configKey);
        if (threshold <= 0) threshold = 4000;
        return assertUnderThreshold(testName, configKey, durationMs, threshold);
    }

    // ── CSV writer ─────────────────────────────────────────────────────────────

    private static void writeToCsv(String testName, String operation,
                                   long durationMs, long thresholdMs, String status) {
        String filePath = ConfigReader.get("perf.trend.log.file");
        if (filePath == null || filePath.isBlank()) filePath = "test-output/performance/perf-trend.csv";

        try {
            Files.createDirectories(Paths.get(filePath).getParent());

            boolean fileExists = Files.exists(Paths.get(filePath));

            try (PrintWriter pw = new PrintWriter(new FileWriter(filePath, true))) {
                if (!fileExists) {
                    pw.println("timestamp,test_name,operation,duration_ms,threshold_ms,status");
                }
                pw.printf("%s,%s,%s,%d,%d,%s%n",
                    LocalDateTime.now().format(TS),
                    testName.replace(",", ";"),
                    operation.replace(",", ";"),
                    durationMs,
                    thresholdMs,
                    status
                );
            }
        } catch (IOException e) {
            log.warn("Could not write to perf trend CSV: {}", e.getMessage());
        }
    }
}
