package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.TickData;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Depth;
import com.zerodhatech.models.LTPQuote;
import com.zerodhatech.models.Quote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KiteMarketService {

    private final KiteSessionManager kiteSessionManager;

    public Map<String, LTPQuote> getLtp(List<String> instruments) throws Exception {
        String[] instrumentArray = instruments.toArray(new String[0]);
        try {
            return kiteSessionManager.getKite().getLTP(instrumentArray);
        } catch (KiteException e) {
            throw new RuntimeException(e);
        }
    }


    public Map<String, Quote> getQuotes(List<String> instruments) throws Exception {
        String[] instrumentArray = instruments.toArray(new String[0]);
        try {
            return kiteSessionManager.getKite().getQuote(instrumentArray);
        } catch (KiteException e) {
            throw new RuntimeException(e);
        }
    }


    public List<TickData> getTickData(List<String> instruments) {

        try {

            String[] instrumentArray = instruments.toArray(new String[0]);

            Map<String, Quote> quotes = kiteSessionManager.getKite().getQuote(instrumentArray);

            List<TickData> ticks = new ArrayList<>();

            for (Map.Entry<String, Quote> entry : quotes.entrySet()) {

                String instrument = entry.getKey();
                Quote quote = entry.getValue();

                long instrumentToken = quote.instrumentToken;

                String tradingSymbol = instrument.split(":")[1];
                String exchange = instrument.split(":")[0];

                double buyQty = 0;
                double sellQty = 0;

                if (quote.depth != null) {
                    buyQty = quote.depth.buy.stream().mapToDouble(Depth::getQuantity).sum();
                    sellQty = quote.depth.sell.stream().mapToDouble(Depth::getQuantity).sum();
                }

                TickData tick = new TickData(
                        instrumentToken,
                        tradingSymbol,
                        quote.lastPrice,
                        quote.ohlc.open,
                        quote.ohlc.high,
                        quote.ohlc.low,
                        quote.ohlc.close,
                        quote.volumeTradedToday,
                        buyQty,
                        sellQty,
                        exchange,
                        null
                );

                ticks.add(tick);
            }

            return ticks;

        } catch (Exception | KiteException e) {
            throw new RuntimeException("Failed to fetch market data", e);
        }
    }
}