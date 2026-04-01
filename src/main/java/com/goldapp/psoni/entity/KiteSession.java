package com.goldapp.psoni.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores one Kite access token per trading day.
 * Only the latest row (today's date) is ever used.
 */
@Entity
@Table(name = "kite_session")
public class KiteSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The Kite access token — valid until ~6 AM next day */
    @Column(name = "access_token", nullable = false, length = 512)
    private String accessToken;

    /** Date this session was created (one per trading day) */
    @Column(name = "session_date", nullable = false, unique = true)
    private LocalDate sessionDate;

    /** Exact timestamp of successful login */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Whether this session is still considered valid */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    // ── Constructors ──────────────────────────────────────────────────────────

    public KiteSession() {}

    public KiteSession(String accessToken) {
        this.accessToken = accessToken;
        this.sessionDate = LocalDate.now();
        this.createdAt   = LocalDateTime.now();
        this.active      = true;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId()                      { return id; }
    public String getAccessToken()           { return accessToken; }
    public LocalDate getSessionDate()        { return sessionDate; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public boolean isActive()               { return active; }

    public void setAccessToken(String t)     { this.accessToken = t; }
    public void setSessionDate(LocalDate d)  { this.sessionDate = d; }
    public void setCreatedAt(LocalDateTime t){ this.createdAt = t; }
    public void setActive(boolean a)         { this.active = a; }
}