package com.goldapp.psoni.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class TickData {

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("instrumentToken")
    private long instrumentToken;

    @JsonProperty("tradingSymbol")
    private String tradingSymbol;

    @JsonProperty("lastPrice")
    private double lastPrice;

    @JsonProperty("change")
    private double change;

    @JsonProperty("changePercent")
    private double changePercent;

    @JsonProperty("open")
    private double open;

    @JsonProperty("high")
    private double high;

    @JsonProperty("low")
    private double low;

    @JsonProperty("close")
    private double close;

    @JsonProperty("volume")
    private double volume;

    @JsonProperty("buyQuantity")
    private double buyQuantity;

    @JsonProperty("sellQuantity")
    private double sellQuantity;

    @JsonProperty("timestamp")
    private long timestamp;


    public TickData() {
    }

    public TickData(long instrumentToken, String tradingSymbol, double lastPrice,
                    double open, double high, double low, double close,
                    double volume, double buyQuantity, double sellQuantity, String exchange) {
        this.instrumentToken = instrumentToken;
        this.tradingSymbol = tradingSymbol;
        this.lastPrice = lastPrice;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.buyQuantity = buyQuantity;
        this.sellQuantity = sellQuantity;
        this.change = lastPrice - close;
        this.changePercent = close != 0 ? ((lastPrice - close) / close) * 100 : 0;
        this.timestamp = Instant.now().toEpochMilli();
        this.exchange = exchange;
    }
}