package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.TickData;
import com.goldapp.psoni.repository.InstrumentRepository;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Tick;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.ticker.OnConnect;
import com.zerodhatech.ticker.OnDisconnect;
import com.zerodhatech.ticker.OnError;
import com.zerodhatech.ticker.OnTicks;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class KiteTickerService {

    private static final Logger log = LoggerFactory.getLogger(KiteTickerService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private KiteSessionManager kiteSessionManager;

    @Autowired
    private InstrumentRepository instrumentRepository; // resolves token → symbol

    private KiteTicker ticker;

    // ── Per-user watchlist registry ───────────────────────────────────────────
    // userId → set of instrument tokens they are watching
    // ConcurrentHashMap so watchlist updates from REST calls are thread-safe
    private final Map<String, Set<Long>> userWatchlists = new ConcurrentHashMap<>();

    // token → symbol name (e.g. 738561 → "NSE:INFY")
    // Populated lazily from InstrumentService
    private final Map<Long, String> symbolCache = new ConcurrentHashMap<>();

    // Last tick per token — snapshot for new page loads
    private final Map<Long, TickData> lastTickCache = new ConcurrentHashMap<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        connectTicker();
    }

    @PreDestroy
    public void shutdown() {
        if (ticker != null && ticker.isConnectionOpen()) {
            ticker.disconnect();
            log.info("KiteTicker disconnected on shutdown");
        }
    }

    // ── Watchlist management (called from your WatchlistController) ───────────

    /**
     * Register or update a user's watchlist.
     * Automatically subscribes any new tokens to the Kite ticker.
     *
     * @param userId   your app's user ID (String to match your auth model)
     * @param tokens   full set of instrument tokens the user wants to watch
     */
    public void updateWatchlist(String userId, Set<Long> tokens) {
        userWatchlists.put(userId, new HashSet<>(tokens));
        ensureSubscribed(tokens);
        log.info("Watchlist updated for user {}: {} tokens", userId, tokens.size());
    }

    /**
     * Call this when a user logs out or disconnects their WebSocket.
     */
    public void removeWatchlist(String userId) {
        userWatchlists.remove(userId);
        log.info("Watchlist removed for user {}", userId);
    }

    // ── Tick processing ───────────────────────────────────────────────────────

    private void processTicks(ArrayList<Tick> ticks) {
        // Index incoming ticks by token — O(1) lookup instead of O(N) scan per user
        Map<Long, Tick> tickIndex = ticks.stream()
                .collect(Collectors.toMap(Tick::getInstrumentToken, t -> t, (a, b) -> b));

        // Update last-tick snapshot cache
        tickIndex.forEach((token, tick) ->
                lastTickCache.put(token, buildTickData(tick)));

        // Push to each user only the symbols in their watchlist
        userWatchlists.forEach((userId, watchedTokens) -> {
            List<TickData> payload = watchedTokens.stream()
                    .filter(tickIndex::containsKey)           // only tokens that ticked this cycle
                    .map(token -> buildTickData(tickIndex.get(token)))
                    .collect(Collectors.toList());

            if (!payload.isEmpty()) {
                // ONE message per user, containing ALL their ticked symbols
                messagingTemplate.convertAndSend("/topic/watchlist/" + userId, payload);
            }
        });
    }

    private TickData buildTickData(Tick tick) {
        String symbol = symbolCache.computeIfAbsent(
                tick.getInstrumentToken(),
                token -> instrumentRepository.findByInstrumentToken(token).getSymbol()   // e.g. "NSE:INFY"
        );

        return new TickData(
                tick.getInstrumentToken(),
                symbol,
                tick.getLastTradedPrice(),
                tick.getOpenPrice(),
                tick.getHighPrice(),
                tick.getLowPrice(),
                tick.getClosePrice(),
                tick.getLastTradedQuantity(),
                tick.getTotalBuyQuantity(),
                tick.getTotalSellQuantity()
        );
    }

    // ── Snapshot endpoint support ─────────────────────────────────────────────

    /**
     * Returns last known ticks for a user's watchlist.
     * Call this from a REST endpoint so the page renders immediately
     * without waiting for the next tick arrival over WebSocket.
     */
    public List<TickData> getSnapshotForUser(String userId) {
        Set<Long> tokens = userWatchlists.getOrDefault(userId, Set.of());
        return tokens.stream()
                .map(lastTickCache::get)
                .collect(Collectors.toList());
    }

    // ── Ticker subscription management ───────────────────────────────────────

    /**
     * Subscribe any tokens not yet tracked by the Kite ticker.
     * Safe to call repeatedly — Kite SDK ignores already-subscribed tokens.
     */
    private void ensureSubscribed(Set<Long> tokens) {
        if (ticker == null || !ticker.isConnectionOpen()) return;

        ArrayList<Long> toSubscribe = new ArrayList<>(tokens);
        ticker.subscribe(toSubscribe);
        ticker.setMode(toSubscribe, KiteTicker.modeLTP); // LTP is enough for a watchlist
        log.debug("Ensured subscription for {} tokens", toSubscribe.size());
    }



    // ── KiteTicker connection ─────────────────────────────────────────────────

    private void connectTicker() {
        try {
            ticker = kiteSessionManager.getOrCreateTicker();

            ticker.setOnConnectedListener(new OnConnect() {
                @Override
                public void onConnected() {
                    log.info("KiteTicker connected");
                    // Re-subscribe all current watchlist tokens on reconnect
                    Set<Long> allTokens = userWatchlists.values().stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());
                    if (!allTokens.isEmpty()) {
                        ensureSubscribed(allTokens);
                    }
                }
            });

            ticker.setOnDisconnectedListener(new OnDisconnect() {
                @Override
                public void onDisconnected() {
                    log.warn("KiteTicker disconnected — will auto-reconnect");
                }
            });

            ticker.setOnErrorListener(new OnError() {
                @Override
                public void onError(Exception e) {
                    log.error("KiteTicker error: {}", e.getMessage(), e);
                }

                @Override
                public void onError(KiteException e) {
                    log.error("KiteTicker KiteException {}: {}", e.code, e.message);
                }

                @Override
                public void onError(String error) {
                    log.error("KiteTicker error: {}", error);
                }
            });

            ticker.setOnTickerArrivalListener(new OnTicks() {
                @Override
                public void onTicks(ArrayList<Tick> ticks) {
                    log.info("Received ticks: {}", ticks.size());
                    processTicks(ticks);
                }
            });

            ticker.setTryReconnection(true);
            ticker.setMaximumRetries(10);
            ticker.setMaximumRetryInterval(30);
            ticker.connect();

        } catch (Exception | KiteException e) {
            log.error("Failed to initialize KiteTicker: {}", e.getMessage(), e);
        }
    }
}