package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.TickData;
import com.goldapp.psoni.dto.WatchlistItemDto;

import java.util.List;

public interface WatchlistService {

    List<Long> tokensForUser(long userId);

    void addSymbol(Long userId, Long instrumentId);

    void removeSymbol(Long userId, Long instrumentId);

    List<TickData> getWatchlist(Long userId) throws Exception;

    List<TickData> getDefaultWatchList() throws Exception;
}