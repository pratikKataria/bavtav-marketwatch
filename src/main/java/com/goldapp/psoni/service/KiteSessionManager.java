package com.goldapp.psoni.service;

import com.goldapp.psoni.entity.KiteSession;
import com.goldapp.psoni.event.SessionRefreshedEvent;
import com.goldapp.psoni.repository.KiteSessionRepository;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.ticker.KiteTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides ready-to-use {@link KiteConnect} and {@link KiteTicker} instances
 * backed by the DB-stored token.
 *
 * <p><b>In-memory cache (KiteConnect)</b>
 * <ul>
 *   <li>On first call, loads today's token from the DB and caches a KiteConnect instance.</li>
 *   <li>Subsequent calls within the same day hit the in-memory cache (no DB round-trip).</li>
 *   <li>{@link #refreshFromDb()} is called by the scheduler after a new login to swap the token.</li>
 *   <li>{@link #invalidate()} forces the next call to reload from DB (used on 403 errors).</li>
 * </ul>
 *
 * <p><b>KiteTicker lifecycle</b>
 * <ul>
 *   <li>{@link #getOrCreateTicker()} lazily creates and connects a ticker for the current session.</li>
 *   <li>The ticker is re-created automatically when the session is refreshed or invalidated.</li>
 *   <li>Call {@link #stopTicker()} to gracefully disconnect the WebSocket (e.g., on shutdown).</li>
 * </ul>
 *
 * <p><b>Thread safety:</b> {@link ReentrantReadWriteLock} allows concurrent reads, exclusive writes.
 */
@Component
public class KiteSessionManager {

    private static final Logger log = LoggerFactory.getLogger(KiteSessionManager.class);

    @Value("${zerodha.api-key}")
    private String apiKey;

    private final KiteSessionRepository sessionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // ── In-memory cache ───────────────────────────────────────────────────────
    private KiteConnect cachedKite;
    private LocalDate   cachedDate;

    /** Lazily created; tied to the current cachedKite session. */
    private KiteTicker cachedTicker;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public KiteSessionManager(KiteSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // ── KiteConnect ───────────────────────────────────────────────────────────

    /**
     * Returns a {@link KiteConnect} instance with a valid access token.
     * Loads from DB if cache is empty or stale.
     *
     * @throws IllegalStateException if no active session found for today
     */
    public KiteConnect getKite() {
        // Fast path — read lock, cache hit
        lock.readLock().lock();
        try {
            if (isCacheValid()) return cachedKite;
        } finally {
            lock.readLock().unlock();
        }

        // Slow path — write lock, load from DB
        lock.writeLock().lock();
        try {
            if (isCacheValid()) return cachedKite; // double-check
            return loadFromDb();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Called by KiteLoginScheduler after a fresh login — swaps the cached token
     * with the newly persisted one and tears down any existing ticker so it is
     * re-created with the new credentials on the next {@link #getOrCreateTicker()} call.
     */
    public void refreshFromDb() {
        lock.writeLock().lock();
        try {
            stopTickerInternal();   // tear down stale ticker before swapping token
            loadFromDb();
            eventPublisher.publishEvent(new SessionRefreshedEvent(this));
            log.info("KiteSessionManager: in-memory cache refreshed from DB");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears the in-memory cache so the next {@link #getKite()} reloads from DB.
     * Also stops and discards the cached ticker.
     * Call this when a Kite API call returns a 403 / TokenException.
     */
    public void invalidate() {
        lock.writeLock().lock();
        try {
            stopTickerInternal();
            cachedKite = null;
            cachedDate = null;
            log.warn("KiteSessionManager: cache invalidated");
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ── KiteTicker ────────────────────────────────────────────────────────────

    /**
     * Returns the existing {@link KiteTicker} for today's session, or creates
     * and connects a new one if none exists yet.
     *
     * <p>The caller is responsible for setting on-tick / on-error / on-connect
     * callbacks <em>before</em> subscribing to tokens. Because {@link KiteTicker}
     * is created lazily here, attach your listeners to the returned instance:
     *
     * <pre>{@code
     * KiteTicker ticker = sessionManager.getOrCreateTicker();
     * ticker.setOnConnectedListener(() -> ticker.subscribe(tokens));
     * ticker.setOnTickerArrivalListener(ticks -> process(ticks));
     * ticker.connect();
     * }</pre>
     *
     * @throws IllegalStateException if no active Kite session exists for today
     */
    public KiteTicker getOrCreateTicker() {
        // Fast path
        lock.readLock().lock();
        try {
            if (cachedTicker != null && isCacheValid()) return cachedTicker;
        } finally {
            lock.readLock().unlock();
        }

        // Slow path — ensure KiteConnect is loaded, then build ticker
        lock.writeLock().lock();
        try {
            if (cachedTicker != null && isCacheValid()) return cachedTicker; // double-check

            KiteConnect kite = isCacheValid() ? cachedKite : loadFromDb();
            cachedTicker = buildTicker(kite);
            log.info("KiteSessionManager: KiteTicker created for session date={}", cachedDate);
            return cachedTicker;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gracefully disconnects and discards the cached {@link KiteTicker}.
     * Safe to call even if no ticker has been created yet.
     * Typically called on application shutdown or before a forced re-login.
     */
    public void stopTicker() {
        lock.writeLock().lock();
        try {
            stopTickerInternal();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns {@code true} if a ticker has been created and is currently connected.
     * Useful for health-check endpoints.
     */
    public boolean isTickerConnected() {
        lock.readLock().lock();
        try {
            return cachedTicker != null && cachedTicker.isConnectionOpen();
        } finally {
            lock.readLock().unlock();
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private boolean isCacheValid() {
        return cachedKite != null && LocalDate.now().equals(cachedDate);
    }

    /** Must be called while holding the write lock. */
    private KiteConnect loadFromDb() {
        Optional<KiteSession> opt =
                sessionRepository.findBySessionDateAndActiveTrue(LocalDate.now());

        if (opt.isEmpty()) {
            throw new IllegalStateException(
                    "No active Kite session found for today (" + LocalDate.now() + "). " +
                    "Scheduler may not have run yet — trigger POST /api/admin/kite/relogin.");
        }

        KiteSession session = opt.get();
        KiteConnect kite = new KiteConnect(apiKey);
        kite.setAccessToken(session.getAccessToken());

        this.cachedKite = kite;
        this.cachedDate = LocalDate.now();

        log.info("Loaded Kite session from DB (id={}, created={})",
                session.getId(), session.getCreatedAt());
        return kite;
    }

    /**
     * Builds a {@link KiteTicker} from the given {@link KiteConnect} instance.
     * Must be called while holding the write lock.
     */
    private KiteTicker buildTicker(KiteConnect kite) {
        return new KiteTicker(kite.getAccessToken(), apiKey);
    }

    /**
     * Stops the cached ticker if it exists.
     * Must be called while holding the write lock.
     */
    private void stopTickerInternal() {
        if (cachedTicker != null) {
            try {
                cachedTicker.disconnect();
                log.info("KiteSessionManager: KiteTicker disconnected");
            } catch (Exception e) {
                log.warn("KiteSessionManager: error while disconnecting KiteTicker — {}", e.getMessage());
            } finally {
                cachedTicker = null;
            }
        }
    }
}