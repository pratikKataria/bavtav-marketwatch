package com.goldapp.psoni.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MarketInstrumentDto {
    private Long id;
    private String symbol;
    private String name;
    private String exchange;
    private String instrumentType;
    private LocalDate expiryDate;
    private Integer lotSize;

    private Double ltp;
    private Double change;
    private Double changePercent;

    private Double prevClose;
    private Double open;
    private Double high;
    private Double low;
}