package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.TickData;
import com.goldapp.psoni.dto.WatchlistItemDto;
import com.goldapp.psoni.entity.InstrumentMaster;
import com.goldapp.psoni.entity.UserWatchlist;
import com.goldapp.psoni.repository.InstrumentRepository;
import com.goldapp.psoni.repository.UserWatchlistRepository;
import com.zerodhatech.models.Quote;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {

    private final UserWatchlistRepository watchlistRepository;
    private final InstrumentRepository instrumentRepository;
    private final KiteMarketService kiteMarketService;
    private final KiteTickerService kiteTickerService;

    @Override
    public void addSymbol(Long userId, Long instrumentId) {

        boolean exists = watchlistRepository
                .findByUserIdAndInstrumentId(userId, instrumentId)
                .isPresent();

        if (exists) {
            throw new RuntimeException("Symbol already in watchlist");
        }

        InstrumentMaster instrument = instrumentRepository
                .findById(instrumentId)
                .orElseThrow(() -> new RuntimeException("Instrument not found"));

        UserWatchlist watchlist = UserWatchlist.builder()
                .userId(userId)
                .instrumentId(instrumentId)
                .exchange(instrument.getExchange())
                .symbol(instrument.getSymbol())
                .displayOrder(0)
                .build();

        watchlistRepository.save(watchlist);
        refreshTickerWatchlist(userId);
    }

    @Override
    @Transactional
    public void removeSymbol(Long userId, Long instrumentId) {
        watchlistRepository.deleteByUserIdAndInstrumentId(userId, instrumentId);
        refreshTickerWatchlist(userId);
    }

    @Override
    public List<TickData> getWatchlist(Long userId) throws Exception {

        List<UserWatchlist> watchlist = watchlistRepository.findByUserIdOrderByDisplayOrderAsc(userId);

        if (watchlist.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> kiteSymbols = watchlist.stream()
                .map(w -> w.getExchange() + ":" + w.getSymbol())
                .toList();

        List<TickData> quotes = kiteMarketService.getTickData(kiteSymbols);
        return quotes;
    }

    @Override
    public List<TickData> getDefaultWatchList() throws Exception {
        List<InstrumentMaster> instruments = instrumentRepository.findByDefaultSymbolTrue();

        if (instruments.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> kiteSymbols = instruments.stream()
                .map(i -> i.getExchange() + ":" + i.getSymbol())
                .toList();


        return kiteMarketService.getTickData(kiteSymbols);
    }

    private void refreshTickerWatchlist(Long userId) {
        try {
            List<UserWatchlist> list = watchlistRepository.findByUserIdOrderByDisplayOrderAsc(userId);
            Set<Long> tokens = new HashSet<>();

            for (UserWatchlist watch : list) {

                Long instrumentId = watch.getInstrumentId();
                System.out.println("InstrumentId: " + instrumentId);

                InstrumentMaster instrument = instrumentRepository.findById(instrumentId)
                        .orElseThrow(() -> new RuntimeException("Instrument not found: " + instrumentId));

                Long token = instrument.getInstrumentToken();

                System.out.println("InstrumentToken: " + token);

                tokens.add(token);
            }

            System.out.println("Final Tokens Set: " + tokens);
            kiteTickerService.updateWatchlist(userId.toString(), tokens);
        } catch (Exception xe) {
            log.error("Unknown error occurred {}", xe.getLocalizedMessage());
        }
    }

    @Override
    public List<Long> tokensForUser(long userId) {
        return watchlistRepository.findByUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(UserWatchlist::getInstrumentId)
                .toList();
    }
}