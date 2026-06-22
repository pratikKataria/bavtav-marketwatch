package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.TickData;
import com.goldapp.psoni.dto.WatchlistItemDto;
import com.goldapp.psoni.entity.InstrumentMaster;
import com.goldapp.psoni.entity.UserWatchlist;
import com.goldapp.psoni.repository.InstrumentRepository;
import com.goldapp.psoni.repository.UserWatchlistRepository;
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
    public List<WatchlistItemDto> getWatchlist(Long userId) throws Exception {

        List<Object[]> rows = watchlistRepository.findWatchlistWithInstrumentByUserId(userId);

        if (rows.isEmpty()) return new ArrayList<>();

        List<String> kiteSymbols = new ArrayList<>();
        Map<String, InstrumentMaster> instrumentMap = new HashMap<>();

        for (Object[] row : rows) {
            UserWatchlist uw = (UserWatchlist) row[0];
            InstrumentMaster im = (InstrumentMaster) row[1];
            String key = im.getExchange() + ":" + im.getSymbol();
            kiteSymbols.add(key);
            instrumentMap.put(key, im);
        }

        List<TickData> ticks = kiteMarketService.getTickData(kiteSymbols);

        return ticks.stream().map(tick -> {
            String key = tick.getExchange() + ":" + tick.getTradingSymbol();
            InstrumentMaster im = instrumentMap.get(key);

            WatchlistItemDto dto = WatchlistItemDto.from(tick);

            if (im != null) {
                dto.setId(im.getId());
                dto.setName(im.getName());
                dto.setInstrumentType(im.getInstrumentType());
                dto.setSegment(im.getSegment());
                dto.setExpiryDate(im.getExpiryDate());
                dto.setStrikePrice(im.getStrikePrice());
                dto.setTickSize(im.getTickSize());
                dto.setLotSize(im.getLotSize());
                dto.setExchangeToken(im.getExchangeToken());
            }

            return dto;
        }).toList();
    }

    private List<TickData> enrichWithInstrumentToken(List<TickData> ticks, List<UserWatchlist> watchlist) {

        Map<String, Long> tokenMap = watchlist.stream()
                .collect(Collectors.toMap(
                        w -> w.getExchange() + ":" + w.getSymbol(),
                        UserWatchlist::getInstrumentId
                ));

        ticks.forEach(tick -> {
            Long token = tokenMap.get(tick.getExchange() + ":" + tick.getTradingSymbol());
            if (token != null) {
                tick.setInstrumentToken(token);
            }

            if (tick.getId() == null) {
                tick.setId(token);
            }
        });

        return ticks;
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