package com.goldapp.psoni.dto;

public interface InstrumentSearchProjection {
    Long   getInstrumentId();  // im.id
    String getSymbol();        // im.symbol
    String getExchange();      // im.exchange
    String getName();          // im.name
    Boolean getSubscribed();   // TRUE when present in user_watchlist
}