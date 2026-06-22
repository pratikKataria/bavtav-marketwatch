package com.goldapp.psoni.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItemDto {

    public static WatchlistItemDto from(TickData tick) {
        WatchlistItemDto dto = new WatchlistItemDto();
        dto.setId(tick.getId());
        dto.setExchange(tick.getExchange());
        dto.setInstrumentToken(tick.getInstrumentToken());
        dto.setTradingSymbol(tick.getTradingSymbol());
        dto.setLastPrice(tick.getLastPrice());
        dto.setChange(tick.getChange());
        dto.setChangePercent(tick.getChangePercent());
        dto.setOpen(tick.getOpen());
        dto.setHigh(tick.getHigh());
        dto.setLow(tick.getLow());
        dto.setClose(tick.getClose());
        dto.setVolume(tick.getVolume());
        dto.setBuyQuantity(tick.getBuyQuantity());
        dto.setSellQuantity(tick.getSellQuantity());
        dto.setTimestamp(tick.getTimestamp());
        return dto;
    }

    // --------- TickData fields (exact same JSON keys mobile uses) ---------
    @JsonProperty("id")
    private Long id;

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

    // --------- Extra fields from InstrumentMaster ---------
    @JsonProperty("name")
    private String name;

    @JsonProperty("instrumentType")
    private String instrumentType;

    @JsonProperty("segment")
    private String segment;

    @JsonProperty("expiryDate")
    private LocalDate expiryDate;

    @JsonProperty("strikePrice")
    private BigDecimal strikePrice;

    @JsonProperty("tickSize")
    private BigDecimal tickSize;

    @JsonProperty("lotSize")
    private Integer lotSize;

    @JsonProperty("exchangeToken")
    private Long exchangeToken;
}