package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.InstrumentMeta;
import com.goldapp.psoni.dto.TickData;
import com.goldapp.psoni.entity.InstrumentMaster;
import com.goldapp.psoni.entity.UserWatchlist;
import com.goldapp.psoni.event.SessionRefreshedEvent;
import com.goldapp.psoni.repository.InstrumentRepository;
import com.goldapp.psoni.repository.UserWatchlistRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.springframework.context.event.EventListener;

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
    private final Map<Long, InstrumentMeta> instrumentCache = new ConcurrentHashMap<>();

    // Last tick per token — snapshot for new page loads
    private final Map<Long, TickData> lastTickCache = new ConcurrentHashMap<>();

    @Autowired
    private UserWatchlistRepository watchlistRepository;


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
     * @param userId your app's user ID (String to match your auth model)
     * @param tokens full set of instrument tokens the user wants to watch
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

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")   // nice ISO-8601 format
            .serializeNulls()                              // keep nulls so nothing is hidden
            .create();

    private TickData buildTickData(Tick tick) {
//        log.info("Full Tick payload → {}", GSON.toJson(tick));
//        String symbol = symbolCache.computeIfAbsent(
//                tick.getInstrumentToken(),
//                token -> instrumentRepository.findByInstrumentToken(token).getSymbol()   // e.g. "NSE:INFY"
//        );

        InstrumentMeta meta = instrumentCache.computeIfAbsent(
                tick.getInstrumentToken(),
                token -> {
                    InstrumentMaster instrument = instrumentRepository.findByInstrumentToken(token);
                    return new InstrumentMeta(
                            instrument.getSymbol(),
                            instrument.getExchange(),
                            instrument.getId()
                    );
                }
        );

        return new TickData(
                tick.getInstrumentToken(),
                meta.symbol(),
                tick.getLastTradedPrice(),
                tick.getOpenPrice(),
                tick.getHighPrice(),
                tick.getLowPrice(),
                tick.getClosePrice(),
                tick.getLastTradedQuantity(),
                tick.getTotalBuyQuantity(),
                tick.getTotalSellQuantity(),
                meta.exchange(),
                meta.id()
        );
    }

    // ── Snapshot endpoint support ─────────────────────────────────────────────

    /**
     * Returns last known ticks for a user's watchlist.
     * Call this from a REST endpoint so the page renders immediately
     * without waiting for the next tick arrival over WebSocket.
     */
    public List<TickData> getSnapshotForUser(Long userId) {
        List<UserWatchlist> list = watchlistRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        Set<Long> tokens = new HashSet<>();

        for (UserWatchlist watch : list) {

            Long instrumentId = watch.getInstrumentId();
            System.out.println("InstrumentId: " + instrumentId);

            InstrumentMaster instrument = instrumentRepository.findById(instrumentId)
                    .orElseThrow(() -> new RuntimeException("Instrument not found: " + instrumentId));

            Long token = instrument.getInstrumentToken();

            System.out.println("InstrumentToken: " + token);

            tokens.add(token);
        }
        updateWatchlist(userId.toString(), tokens);
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
        ticker.setMode(toSubscribe, KiteTicker.modeFull); // LTP is enough for a watchlist
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
//                    log.info("Received ticks: {}", ticks.size());
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

    public void reconnectTicker() {
        log.info("Reconnecting KiteTicker with fresh session...");
        connectTicker();   // wires all listeners onto the new ticker instance
    }

    @EventListener
    public void onSessionRefreshed(SessionRefreshedEvent event) {
        log.info("Session refreshed event received — reconnecting ticker");
        instrumentCache.clear();
        lastTickCache.clear();
        userWatchlists.clear();
        shutdown();
        reconnectTicker();
    }

}