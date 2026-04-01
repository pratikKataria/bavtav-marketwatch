package com.goldapp.psoni.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WatchlistItemDto {

    private Long instrumentId;
    private String symbol;
    private String exchange;

    private Double ltp;
    private Double change;
    private Double changePercent;
}