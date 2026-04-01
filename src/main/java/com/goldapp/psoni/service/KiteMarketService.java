package com.goldapp.psoni.service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.LTPQuote;
import com.zerodhatech.models.Quote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}