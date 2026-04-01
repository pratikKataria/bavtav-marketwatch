package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.InstrumentSearchDto;
import com.goldapp.psoni.dto.InstrumentSearchProjection;
import com.goldapp.psoni.dto.InstrumentSearchResponseDto;

import java.util.List;

public interface InstrumentSearchService {
    List<InstrumentSearchDto> search(String exchange, String query, Long userId);
}