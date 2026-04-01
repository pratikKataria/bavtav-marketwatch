package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.WatchlistItemDto;

import java.util.List;

public interface WatchlistService {

    void addSymbol(Long userId, Long instrumentId);

    void removeSymbol(Long userId, Long instrumentId);

    List<WatchlistItemDto> getWatchlist(Long userId) throws Exception;
}