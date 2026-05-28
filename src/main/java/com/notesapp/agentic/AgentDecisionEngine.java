package com.notesapp.agentic;

import com.notesapp.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AgentDecisionEngine — Section 3.3
 *
 * The "brain" of the agentic layer.
 *
 * When a UI action throws an exception, this engine evaluates context and decides:
 *   RETRY          → try the same action again (stale element, transient issue)
 *   RETRY_WITH_JS  → retry using JavaScript click (element blocked/intercepted)
 *   SKIP           → mark as known flaky, don't fail the test
 *   FAIL           → genuine failure — propagate the exception
 *
 * This is what separates "agentic" automation from plain retry logic:
 * the engine makes a context-aware decision rather than blindly looping.
 */
public class AgentDecisionEngine {

    private static final Logger log = LoggerFactory.getLogger(AgentDecisionEngine.class);

    public enum Decision {
        RETRY,
        RETRY_WITH_JS,
        SKIP,
        FAIL
    }

    private final int maxRetries;
    private int attemptCount = 0;

    public AgentDecisionEngine() {
        int cfg = ConfigReader.getInt("agentic.max.retries");
        this.maxRetries = cfg > 0 ? cfg : 3;
    }

    /**
     * Decide what to do next based on the exception and attempt count.
     *
     * @param ex       the exception thrown by the failed step
     * @param stepName human-readable label of the step (for logging)
     * @return         the Decision the caller should act on
     */
    public Decision evaluate(Exception ex, String stepName) {
        attemptCount++;
        String exType = ex.getClass().getSimpleName();

        log.warn("AgentDecisionEngine — step='{}' attempt={} exception={}: {}",
                stepName, attemptCount, exType, ex.getMessage());

        // Hard limit — stop after maxRetries
        if (attemptCount >= maxRetries) {
            log.error("Step '{}' failed {} time(s). Decision: FAIL", stepName, attemptCount);
            return Decision.FAIL;
        }

        // StaleElementReferenceException — element was re-rendered, just retry
        if (exType.contains("StaleElement")) {
            log.info("StaleElement on '{}' — Decision: RETRY (attempt {})", stepName, attemptCount);
            return Decision.RETRY;
        }

        // ElementClickInterceptedException / ElementNotInteractable — try JS click
        if (exType.contains("ElementClickIntercepted") || exType.contains("ElementNotInteractable")) {
            log.info("Click blocked on '{}' — Decision: RETRY_WITH_JS", stepName);
            return Decision.RETRY_WITH_JS;
        }

        // TimeoutException / NoSuchElement — wait and retry
        if (exType.contains("Timeout") || exType.contains("NoSuchElement")) {
            log.info("Timeout/NoSuchElement on '{}' — Decision: RETRY (attempt {})", stepName, attemptCount);
            return Decision.RETRY;
        }

        // First unknown failure → retry once; second → fail
        if (attemptCount < 2) {
            log.info("Unknown exception on '{}' — one more attempt. Decision: RETRY", stepName);
            return Decision.RETRY;
        }

        log.error("Unrecoverable on '{}' after {} attempt(s). Decision: FAIL", stepName, attemptCount);
        return Decision.FAIL;
    }

    /** Reset between logical test steps so retry count doesn't bleed across steps. */
    public void reset() { attemptCount = 0; }

    public int getAttemptCount() { return attemptCount; }
}
