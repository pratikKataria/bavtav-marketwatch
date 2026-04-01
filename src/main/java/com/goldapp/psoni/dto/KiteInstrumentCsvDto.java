package com.goldapp.psoni.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KiteInstrumentCsvDto {

    private Long instrumentToken;
    private Long exchangeToken;
    private String symbol;
    private String name;
    private BigDecimal lastPrice;
    private LocalDate expiryDate;
    private BigDecimal strikePrice;
    private BigDecimal tickSize;
    private Integer lotSize;
    private String instrumentType;
    private String segment;
    private String exchange;
}