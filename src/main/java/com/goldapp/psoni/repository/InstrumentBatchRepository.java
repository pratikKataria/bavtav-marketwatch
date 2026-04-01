package com.goldapp.psoni.repository;

import com.goldapp.psoni.dto.KiteInstrumentCsvDto;

import java.util.List;

public interface InstrumentBatchRepository {

    int[] batchUpsert(List<KiteInstrumentCsvDto> records);

    void markMissingInstrumentsInactive(List<String> currentKeys);
}