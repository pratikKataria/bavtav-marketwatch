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
    private InstrumentRepository instrumentRepository;

    @Autowired
    private UserWatchlistRepository watchlistRepository;

    private KiteTicker ticker;

    private final Map<String, Set<Long>> userWatchlists = new ConcurrentHashMap<>();
    private final Map<Long, InstrumentMeta> instrumentCache = new ConcurrentHashMap<>();
    private final Map<Long, TickData> lastTickCache = new ConcurrentHashMap<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        connectTicker();
    }

    @PreDestroy
    public void shutdown() {
        if (ticker != null) {
            try {
                if (ticker.isConnectionOpen()) {
                    ticker.disconnect();
                    log.info("KiteTicker disconnected on shutdown");
                }
            } catch (Exception e) {
                log.warn("Error during ticker disconnect: {}", e.getMessage());
            } finally {
                ticker = null; // FIX: always null out so stale instance is never reused
            }
        }
    }

    // ── Watchlist management ──────────────────────────────────────────────────

    public void updateWatchlist(String userId, Set<Long> tokens) {
        userWatchlists.put(userId, new HashSet<>(tokens));
        ensureSubscribed(tokens);
        log.info("Watchlist updated for user {}: {} tokens", userId, tokens.size());
    }

    public void removeWatchlist(String userId) {
        userWatchlists.remove(userId);
        log.info("Watchlist removed for user {}", userId);
    }

    // ── Tick processing ───────────────────────────────────────────────────────

    private void processTicks(ArrayList<Tick> ticks) {
        Map<Long, Tick> tickIndex = ticks.stream()
                .collect(Collectors.toMap(Tick::getInstrumentToken, t -> t, (a, b) -> b));

        tickIndex.forEach((token, tick) -> {
            TickData td = buildTickData(tick);
            if (td != null) { // FIX: guard against null from buildTickData
                lastTickCache.put(token, td);
            }
        });

        userWatchlists.forEach((userId, watchedTokens) -> {
            List<TickData> payload = watchedTokens.stream()
                    .filter(tickIndex::containsKey)
                    .map(token -> buildTickData(tickIndex.get(token)))
                    .filter(Objects::nonNull) // FIX: drop any null tick entries
                    .collect(Collectors.toList());

            if (!payload.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/watchlist/" + userId, payload);
            }
        });
    }

    private TickData buildTickData(Tick tick) {
        if (tick == null) return null;

        try {
            InstrumentMeta meta = instrumentCache.computeIfAbsent(
                    tick.getInstrumentToken(),
                    token -> {
                        // FIX: null-safe DB lookup
                        InstrumentMaster instrument = instrumentRepository.findByInstrumentToken(token);
                        if (instrument == null) {
                            log.warn("No instrument found for token {}, skipping", token);
                            return null;
                        }
                        return new InstrumentMeta(
                                instrument.getSymbol(),
                                instrument.getExchange(),
                                instrument.getId()
                        );
                    }
            );

            // FIX: if meta is null (unknown token), skip this tick entirely
            if (meta == null) {
                instrumentCache.remove(tick.getInstrumentToken()); // don't cache the null
                return null;
            }

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
        } catch (Exception e) {
            log.error("Failed to build TickData for token {}: {}", tick.getInstrumentToken(), e.getMessage());
            return null;
        }
    }

    // ── Snapshot endpoint ─────────────────────────────────────────────────────

    public List<TickData> getSnapshotForUser(Long userId) {
        List<UserWatchlist> list = watchlistRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        Set<Long> tokens = new HashSet<>();

        for (UserWatchlist watch : list) {
            Long instrumentId = watch.getInstrumentId();
            InstrumentMaster instrument = instrumentRepository.findById(instrumentId)
                    .orElse(null);

            if (instrument == null) {
                log.warn("Instrument not found for id {}, skipping", instrumentId);
                continue; // FIX: skip missing instruments instead of throwing
            }

            Long token = instrument.getInstrumentToken();
            tokens.add(token);

            // FIX: warm the instrumentCache eagerly so buildTickData never hits DB cold
            instrumentCache.computeIfAbsent(token, t ->
                    new InstrumentMeta(
                            instrument.getSymbol(),
                            instrument.getExchange(),
                            instrument.getId()
                    )
            );
        }

        updateWatchlist(userId.toString(), tokens);

        // FIX: filter out nulls — tokens with no ticks yet return null from lastTickCache
        List<TickData> snapshot = tokens.stream()
                .map(lastTickCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Snapshot for user {}: {}/{} tokens have cached ticks",
                userId, snapshot.size(), tokens.size());

        return snapshot;
    }

    // ── Subscription management ───────────────────────────────────────────────

    private void ensureSubscribed(Set<Long> tokens) {
        if (ticker == null || !ticker.isConnectionOpen()) {
            log.warn("ensureSubscribed called but ticker is not connected — tokens will be subscribed on next connect");
            return;
        }

        ArrayList<Long> toSubscribe = new ArrayList<>(tokens);
        ticker.subscribe(toSubscribe);
        ticker.setMode(toSubscribe, KiteTicker.modeFull);
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
                    // FIX: re-subscribe ALL current watchlist tokens on every connect/reconnect
                    Set<Long> allTokens = userWatchlists.values().stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());
                    if (!allTokens.isEmpty()) {
                        ensureSubscribed(allTokens);
                        log.info("Re-subscribed {} tokens on connect", allTokens.size());
                    } else {
                        log.info("No tokens to re-subscribe on connect");
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
                    processTicks(ticks);
                }
            });

            ticker.setTryReconnection(true);
            ticker.setMaximumRetries(10);
            ticker.setMaximumRetryInterval(30);
            ticker.connect();

        } catch (Exception | KiteException e) {
            log.error("Failed to initialize KiteTicker: {}", e.getMessage(), e);
            ticker = null; // FIX: null out on failure so shutdown() doesn't reference a bad instance
        }
    }

    public void reconnectTicker() {
        log.info("Reconnecting KiteTicker with fresh session...");
        connectTicker();
    }

    @EventListener
    public void onSessionRefreshed(SessionRefreshedEvent event) {
        log.info("Session refreshed event received — reconnecting ticker");
        instrumentCache.clear();
        lastTickCache.clear();
        // FIX: do NOT clear userWatchlists here — we need them in onConnected
        // to re-subscribe tokens after the new ticker connects
        shutdown();
        reconnectTicker();
    }
}