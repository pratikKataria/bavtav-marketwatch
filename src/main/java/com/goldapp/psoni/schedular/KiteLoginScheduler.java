package com.goldapp.psoni.schedular;

import com.goldapp.psoni.service.KiteLoginService;
import com.goldapp.psoni.service.KiteSessionManager;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the Kite login flow every morning at 7:00 AM IST (01:30 UTC).
 *
 * Flow:
 *   1. KiteLoginService performs full 3-step login and saves token to DB
 *   2. KiteSessionManager refreshes its in-memory cache from the new DB row
 *
 * The scheduler also retries up to 3 times (with 2-min gaps) if the login fails —
 * common on days when Zerodha's servers are slow at market open.
 */
@Component
public class KiteLoginScheduler {

    private static final Logger log = LoggerFactory.getLogger(KiteLoginScheduler.class);

    private static final int MAX_RETRIES    = 3;
    private static final long RETRY_DELAY_MS = 2 * 60 * 1000L; // 2 minutes

    private final KiteLoginService loginService;
    private final KiteSessionManager sessionManager;

    public KiteLoginScheduler(KiteLoginService loginService,
                               KiteSessionManager sessionManager) {
        this.loginService   = loginService;
        this.sessionManager = sessionManager;
    }

    /**
     * Cron: 01:30 UTC = 07:00 AM IST
     * Runs Monday–Friday only (markets are closed weekends).
     *
     * To also run on Saturday for testing: replace "MON-FRI" with "*"
     */
    @Scheduled(cron = "0 30 1 * * MON-FRI", zone = "UTC")
    public void scheduledLogin() {
        log.info("=== Kite scheduled login started ===");
        attemptLoginWithRetry();
    }

    /**
     * Exposed for manual trigger from AdminController.
     * Not a scheduled method — called on demand.
     */
    public void triggerManualLogin() {
        log.info("=== Kite manual login triggered ===");
        attemptLoginWithRetry();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void attemptLoginWithRetry() {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                loginService.loginAndPersist();
                sessionManager.refreshFromDb();
                log.info("Kite login succeeded on attempt {}/{}", attempt, MAX_RETRIES);
                return;
            } catch (Exception e) {
                log.error("Kite login attempt {}/{} failed: {}", attempt, MAX_RETRIES, e.getMessage(), e);

                if (attempt < MAX_RETRIES) {
                    log.info("Retrying in {} minutes...", RETRY_DELAY_MS / 60000);
                    sleep(RETRY_DELAY_MS);
                } else {
                    log.error("All {} login attempts exhausted. " +
                              "Manual intervention required — POST /api/admin/kite/relogin", MAX_RETRIES);
                }
            } catch (KiteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}