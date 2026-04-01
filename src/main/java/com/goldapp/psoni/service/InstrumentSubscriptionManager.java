package com.goldapp.psoni.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class InstrumentSubscriptionManager {

    private final Map<Long, Set<String>> instrumentSubscribers = new ConcurrentHashMap<>();

    private final KiteSessionManager kiteSessionManager;

    public synchronized void subscribe(Long token, String sessionId) {
        instrumentSubscribers.computeIfAbsent(token, k -> new HashSet<>())
                .add(sessionId);

        if (instrumentSubscribers.get(token).size() == 1) {
            kiteSessionManager.getOrCreateTicker().subscribe((ArrayList<Long>) Collections.singletonList(token));
            System.out.println("Subscribed to token " + token);
        }
    }

    public synchronized void unsubscribe(Long token, String sessionId) {

        Set<String> sessions = instrumentSubscribers.get(token);

        if (sessions == null) return;

        sessions.remove(sessionId);

        if (sessions.isEmpty()) {

            kiteSessionManager.getOrCreateTicker().unsubscribe((ArrayList<Long>) Collections.singletonList(token));

            instrumentSubscribers.remove(token);

            System.out.println("Unsubscribed token " + token);
        }
    }
}