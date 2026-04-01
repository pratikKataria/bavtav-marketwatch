package com.goldapp.psoni.controller;

import com.goldapp.psoni.entity.KiteSession;
import com.goldapp.psoni.repository.KiteSessionRepository;
import com.goldapp.psoni.schedular.KiteLoginScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Admin endpoints for Kite session management.
 * <p>
 * ⚠ Secure all /api/admin/** routes with Spring Security in production.
 * <p>
 * POST /api/admin/kite/relogin     — trigger a fresh login immediately
 * GET  /api/admin/kite/session     — check today's session status in DB
 */
@RestController
@RequestMapping("/api/admin/kite")
public class AdminController {

    private final KiteLoginScheduler scheduler;
    private final KiteSessionRepository sessionRepository;

    public AdminController(KiteLoginScheduler scheduler, KiteSessionRepository sessionRepository) {
        this.scheduler = scheduler;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Manually trigger a fresh Kite login (runs the same flow as the scheduler).
     * Useful for: holidays, server restarts, token corruption, Zerodha API issues.
     */
    @PostMapping("/relogin")
    public ResponseEntity<?> relogin() {
        try {
            scheduler.triggerManualLogin();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Kite login completed and token saved to DB"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Returns today's session status from the DB.
     * Shows whether the scheduler has run and when the token was created.
     */
    @GetMapping("/session")
    public ResponseEntity<?> sessionStatus() {
        Optional<KiteSession> session =
                sessionRepository.findBySessionDateAndActiveTrue(LocalDate.now());

        if (session.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "status", "NO_SESSION",
                    "date", LocalDate.now().toString(),
                    "message", "No active session for today — scheduler hasn't run or failed"
            ));
        }

        KiteSession s = session.get();
        return ResponseEntity.ok(Map.of(
                "status", "ACTIVE",
                "sessionId", s.getId(),
                "date", s.getSessionDate().toString(),
                "createdAt", s.getCreatedAt().toString(),
                // Mask the token in the response — show only last 6 chars for debugging
                "tokenTail", "..." + s.getAccessToken().substring(
                        Math.max(0, s.getAccessToken().length() - 6))
        ));
    }
}