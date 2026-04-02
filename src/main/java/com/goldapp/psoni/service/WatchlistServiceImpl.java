package com.goldapp.psoni.service;

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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistServiceImpl implements WatchlistService {

    private final UserWatchlistRepository watchlistRepository;
    private final InstrumentRepository instrumentRepository;
    private final KiteMarketService kiteMarketService;
    private final MarketService marketService;
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
    public List<WatchlistItemDto> getWatchlist(Long userId) throws Exception {

        List<UserWatchlist> watchlist = watchlistRepository.findByUserIdOrderByDisplayOrderAsc(userId);

        if (watchlist.isEmpty()) {
            return marketService.getDefaultMarketSymbols().stream().map(
                    item -> {
                        return WatchlistItemDto.builder()
                                .instrumentId(item.getId())
                                .symbol(item.getSymbol())
                                .exchange(item.getExchange())
                                .ltp(item.getLtp())
                                .change(item.getChange())
                                .changePercent(null)
                                .build();
                    }
            ).toList();
        }

        List<String> kiteSymbols = watchlist.stream()
                .map(w -> w.getExchange() + ":" + w.getSymbol())
                .toList();

        Map<String, Quote> quotes = kiteMarketService.getQuotes(kiteSymbols);

        return watchlist.stream().map(item -> {

            String key = item.getExchange() + ":" + item.getSymbol();

            Quote quote = quotes.get(key);

            return WatchlistItemDto.builder()
                    .instrumentId(item.getInstrumentId())
                    .symbol(item.getSymbol())
                    .exchange(item.getExchange())
                    .ltp(quote != null ? quote.lastPrice : null)
                    .change(quote != null ? quote.change : null)
                    .changePercent(null)
                    .build();

        }).toList();
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