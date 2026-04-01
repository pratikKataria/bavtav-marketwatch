package com.goldapp.psoni.controller;

import com.goldapp.psoni.dto.InstrumentSearchDto;
import com.goldapp.psoni.dto.InstrumentSearchResponseDto;
import com.goldapp.psoni.service.InstrumentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentSearchService instrumentSearchService;

    @GetMapping("/search")
    public List<InstrumentSearchDto> searchInstruments(
            @RequestHeader("userId") Long userId,
            @RequestParam String exchange,
            @RequestParam String query) {

        return instrumentSearchService.search(exchange, query, userId);
    }
}