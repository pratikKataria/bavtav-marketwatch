package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.InstrumentSearchDto;
import com.goldapp.psoni.dto.InstrumentSearchResponseDto;
import com.goldapp.psoni.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrumentSearchServiceImpl implements InstrumentSearchService {

    private final InstrumentRepository instrumentRepository;

    @Override
    public List<InstrumentSearchDto> search(String exchange, String query, Long userId) {
        List<InstrumentSearchDto> rows = instrumentRepository.searchInstruments(exchange, query, userId);
        return rows;
    }
}