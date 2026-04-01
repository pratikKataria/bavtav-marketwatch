package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.MarketInstrumentDto;
import com.goldapp.psoni.entity.InstrumentMaster;
import com.goldapp.psoni.repository.InstrumentRepository;
import com.zerodhatech.models.Quote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final InstrumentRepository instrumentRepository;
    private final KiteMarketService kiteMarketService;

    public List<MarketInstrumentDto> getDefaultMarketSymbols() throws Exception {

        List<InstrumentMaster> instruments = instrumentRepository.findByDefaultSymbolTrue();

        if (instruments.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> kiteSymbols = instruments.stream()
                .map(i -> i.getExchange() + ":" + i.getSymbol())
                .toList();

        Map<String, Quote> quotes = kiteMarketService.getQuotes(kiteSymbols);

        return instruments.stream().map(inst -> {

            String key = inst.getExchange() + ":" + inst.getSymbol();

            Quote quote = quotes.get(key);

            return MarketInstrumentDto.builder()
                    .id(inst.getId())
                    .symbol(inst.getSymbol())
                    .name(inst.getName())
                    .exchange(inst.getExchange())
                    .instrumentType(inst.getInstrumentType())
                    .expiryDate(inst.getExpiryDate())
                    .lotSize(inst.getLotSize())

                    .ltp(quote != null ? quote.lastPrice : null)
                    .prevClose(quote != null ? quote.ohlc.close : null)
                    .open(quote != null ? quote.ohlc.open : null)
                    .high(quote != null ? quote.ohlc.high : null)
                    .low(quote != null ? quote.ohlc.low : null)

                    .build();
        }).toList();
    }


}