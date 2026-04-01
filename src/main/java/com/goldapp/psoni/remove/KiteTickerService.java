//package com.goldapp.psoni.remove;
//
//import com.goldapp.psoni.dto.TickData;
//import com.goldapp.psoni.service.KiteSessionManager;
//import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
//import com.zerodhatech.models.Tick;
//import com.zerodhatech.ticker.*;
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//public class KiteTickerService {
//
//    private static final Logger log = LoggerFactory.getLogger(KiteTickerService.class);
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    @Autowired
//    private KiteSessionManager kiteSessionManager;
//
//    // Comma-separated instrument tokens to subscribe to, e.g. "408065,738561,884737"
//    @Value("${kite.instrument-tokens}")
//    private String instrumentTokensConfig;
//
//    // Symbol map: token -> trading symbol for display
//    // In production, fetch this from Kite instruments API and cache it
//    @Value("#{${kite.symbol-map}}")
//    private Map<Long, String> symbolMap;
//
//    private KiteTicker ticker;
//
//    // Cache last known tick so new frontend subscribers get an instant snapshot
//    private final Map<Long, TickData> lastTickCache = new ConcurrentHashMap<>();
//
//    @PostConstruct
//    public void init() {
//        connectTicker();
//    }
//
//    private void connectTicker() {
//        try {
//
//            ticker = kiteSessionManager.getOrCreateTicker();
//
//            ticker.setOnConnectedListener(new OnConnect() {
//                @Override
//                public void onConnected() {
//                    log.info("KiteTicker connected. Subscribing to instruments...");
//                    subscribeInstruments();
//                }
//            });
//
//            ticker.setOnDisconnectedListener(new OnDisconnect() {
//                @Override
//                public void onDisconnected() {
//                    log.warn("KiteTicker disconnected. Will auto-reconnect...");
//                }
//            });
//
//            ticker.setOnErrorListener(new OnError() {
//                @Override
//                public void onError(Exception e) {
//                    log.error("KiteTicker error: {}", e.getMessage(), e);
//                }
//
//                @Override
//                public void onError(KiteException e) {
//                    log.error("KiteTicker KiteException: {} - {}", e.code, e.message, e);
//                }
//
//                @Override
//                public void onError(String error) {
//                    log.error("KiteTicker string error: {}", error);
//                }
//            });
//
//            ticker.setOnTickerArrivalListener(new OnTicks() {
//                @Override
//                public void onTicks(ArrayList<Tick> ticks) {
//                    processTicks(ticks);
//                }
//            });
//
//            ticker.setTryReconnection(true);
//            ticker.setMaximumRetries(10);
//            ticker.setMaximumRetryInterval(30);
//
//            ticker.connect();
//
//        } catch (Exception | KiteException e) {
//            log.error("Failed to initialize KiteTicker: {}", e.getMessage(), e);
//        }
//    }
//
//    private void subscribeInstruments() {
//        try {
//            ArrayList<Long> tokens = new ArrayList<>();
//            for (String token : instrumentTokensConfig.split(",")) {
//                tokens.add(Long.parseLong(token.trim()));
//            }
//
//            ticker.subscribe(tokens);
//            // FULL mode gives you OHLC + depth. Use LTP mode if you only need last price.
//            ticker.setMode(tokens, KiteTicker.modeFull);
//
//            log.info("Subscribed to {} instruments in FULL mode", tokens.size());
//        } catch (Exception e) {
//            log.error("Failed to subscribe instruments: {}", e.getMessage(), e);
//        }
//    }
//
//    private void processTicks(ArrayList<Tick> ticks) {
//        for (Tick tick : ticks) {
//            try {
//                String symbol = symbolMap.getOrDefault(tick.getInstrumentToken(),
//                        "TOKEN_" + tick.getInstrumentToken());
//
//                TickData tickData = new TickData(
//                        tick.getInstrumentToken(),
//                        symbol,
//                        tick.getLastTradedPrice(),
//                        tick.getOpenPrice(),
//                        tick.getHighPrice(),
//                        tick.getLowPrice(),
//                        tick.getClosePrice(),
//                        tick.getLastTradedQuantity(),
//                        tick.getTotalBuyQuantity(),
//                        tick.getTotalSellQuantity()
//                );
//
//                // Cache for late joiners
//                lastTickCache.put(tick.getInstrumentToken(), tickData);
//
//                // Broadcast to all subscribed frontend clients
//                // Topic per instrument: /topic/ticks/{token}
//                messagingTemplate.convertAndSend("/topic/ticks/" + tick.getInstrumentToken(), tickData);
//
//                // Also broadcast to a combined feed for watchlist views
//                messagingTemplate.convertAndSend("/topic/ticks/all", tickData);
//
//            } catch (Exception e) {
//                log.error("Error processing tick for token {}: {}",
//                        tick.getInstrumentToken(), e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Returns cached last tick for a token.
//     * Used by REST endpoint so new page loads show price immediately
//     * without waiting for next tick arrival.
//     */
//    public TickData getLastTick(long instrumentToken) {
//        return lastTickCache.get(instrumentToken);
//    }
//
//    public Map<Long, TickData> getAllLastTicks() {
//        return lastTickCache;
//    }
//
//    @PreDestroy
//    public void shutdown() {
//        if (ticker != null && ticker.isConnectionOpen()) {
//            ticker.disconnect();
//            log.info("KiteTicker disconnected on shutdown");
//        }
//    }
//}