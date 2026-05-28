package com.notesapp.tests.performance;

import com.notesapp.api.AuthApi;
import com.notesapp.api.NotesApi;
import com.notesapp.utils.PerformanceLogger;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * PerformanceTest — Section 3.5
 *
 * Pure TestNG performance tests (no Cucumber / no browser).
 * Measures API response times and asserts they stay under
 * the thresholds defined in config.properties:
 *
 *   perf.api.get.notes.ms    = 2000
 *   perf.api.post.note.ms    = 2000
 *   perf.api.delete.note.ms  = 2000
 *
 * Each result is also appended to:
 *   test-output/performance/perf-trend.csv
 *
 * Referenced by:  testng.xml           (full suite)
 *                 testng-parallel.xml  (parallel suite)
 */
public class PerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(PerformanceTest.class);
    private String token;
    private String createdNoteId;

    // ── Setup ─────────────────────────────────────────────────────────────────

    @BeforeClass
    public void setUp() {
        log.info("PerformanceTest — authenticating via API...");
        token = AuthApi.loginAndGetToken();
        Assert.assertNotNull(token, "Auth token must not be null");
        log.info("PerformanceTest — token acquired successfully");
    }

    // ── TC-PERF-01: GET /notes ────────────────────────────────────────────────

    /**
     * TC-PERF-01
     * GET /notes must respond within 2000 ms (perf.api.get.notes.ms).
     */
    @Test(priority = 1, description = "TC-PERF-01: GET /notes response time under threshold")
    public void testGetNotesResponseTime() {
        log.info("TC-PERF-01: Measuring GET /notes response time...");

        Response response = NotesApi.getNotes(token);
        long durationMs = response.time();

        Assert.assertEquals(response.statusCode(), 200,
                "GET /notes must return HTTP 200 before perf check");

        boolean passed = PerformanceLogger.assertApiTime(
                "TC-PERF-01", "perf.api.get.notes.ms", durationMs);

        Assert.assertTrue(passed,
                "TC-PERF-01 FAILED: GET /notes took " + durationMs + " ms — exceeded threshold");
    }

    // ── TC-PERF-02: POST /notes ───────────────────────────────────────────────

    /**
     * TC-PERF-02
     * POST /notes must respond within 2000 ms (perf.api.post.note.ms).
     * Stores the created note ID for deletion in TC-PERF-03.
     */
    @Test(priority = 2, description = "TC-PERF-02: POST /notes response time under threshold")
    public void testCreateNoteResponseTime() {
        log.info("TC-PERF-02: Measuring POST /notes response time...");

        Response response = NotesApi.createNote(
                token, "Work", "Perf Test Note", "Created by PerformanceTest");
        long durationMs = response.time();

        Assert.assertEquals(response.statusCode(), 200,
                "POST /notes must return HTTP 200 before perf check");

        // Store ID for cleanup in TC-PERF-03
        createdNoteId = response.jsonPath().getString("data.id");
        Assert.assertNotNull(createdNoteId, "Created note ID must not be null");

        boolean passed = PerformanceLogger.assertApiTime(
                "TC-PERF-02", "perf.api.post.note.ms", durationMs);

        Assert.assertTrue(passed,
                "TC-PERF-02 FAILED: POST /notes took " + durationMs + " ms — exceeded threshold");
    }

    // ── TC-PERF-03: DELETE /notes/:id ────────────────────────────────────────

    /**
     * TC-PERF-03
     * DELETE /notes/:id must respond within 2000 ms (perf.api.delete.note.ms).
     * Also cleans up the note created in TC-PERF-02.
     * Depends on TC-PERF-02 having run first (priority = 3).
     */
    @Test(priority = 3,
          dependsOnMethods = "testCreateNoteResponseTime",
          description = "TC-PERF-03: DELETE /notes/:id response time under threshold")
    public void testDeleteNoteResponseTime() {
        log.info("TC-PERF-03: Measuring DELETE /notes/{} response time...", createdNoteId);

        Response response = NotesApi.deleteNote(token, createdNoteId);
        long durationMs = response.time();

        Assert.assertEquals(response.statusCode(), 200,
                "DELETE /notes/:id must return HTTP 200 before perf check");

        boolean passed = PerformanceLogger.assertApiTime(
                "TC-PERF-03", "perf.api.delete.note.ms", durationMs);

        Assert.assertTrue(passed,
                "TC-PERF-03 FAILED: DELETE /notes/:id took " + durationMs + " ms — exceeded threshold");
    }
}
