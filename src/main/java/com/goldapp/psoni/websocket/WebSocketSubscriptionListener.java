package com.goldapp.psoni.websocket;

import com.goldapp.psoni.service.InstrumentSubscriptionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@RequiredArgsConstructor
public class WebSocketSubscriptionListener {

    private final InstrumentSubscriptionManager subscriptionManager;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {

        String destination = (String) event.getMessage()
                .getHeaders()
                .get("simpDestination");

        String sessionId = (String) event.getMessage()
                .getHeaders()
                .get("simpSessionId");

        if (destination != null && destination.startsWith("/topic/ticks/")) {

            Long token = Long.parseLong(destination.replace("/topic/ticks/", ""));

            subscriptionManager.subscribe(token, sessionId);
        }
    }

    @EventListener
    public void handleUnsubscribe(SessionUnsubscribeEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        System.out.println("Unsubscribed session: " + sessionId);        String destination = (String) event.getMessage()
                .getHeaders()
                .get("simpDestination");

        if (destination != null && destination.startsWith("/topic/ticks/")) {

            Long token = Long.parseLong(destination.replace("/topic/ticks/", ""));

            subscriptionManager.unsubscribe(token, sessionId);
        }
    }
}