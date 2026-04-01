package com.goldapp.psoni.dto;

public record InstrumentSearchDto(
        Long instrumentId,
        String symbol,
        String exchange,
        String name,
        Boolean subscribed) {
}
