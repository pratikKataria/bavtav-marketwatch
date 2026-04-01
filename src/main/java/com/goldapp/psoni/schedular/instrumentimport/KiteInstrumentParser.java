package com.goldapp.psoni.schedular.instrumentimport;

import com.goldapp.psoni.dto.KiteInstrumentCsvDto;

import java.util.List;

public interface KiteInstrumentParser {
    List<KiteInstrumentCsvDto> parse(byte[] csvBytes);
}