package com.goldapp.psoni.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InstrumentSearchResponseDto {

    private Long instrumentId;
    private String symbol;
    private String exchange;
    private String name;
}