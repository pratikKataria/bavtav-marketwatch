package com.goldapp.psoni.controller;

import com.goldapp.psoni.dto.InstrumentSyncResponseDto;
import com.goldapp.psoni.service.KiteInstrumentSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/instruments/sync")
@RequiredArgsConstructor
public class InstrumentSyncController {

    private final KiteInstrumentSyncService syncService;

    @PostMapping
    public ResponseEntity<InstrumentSyncResponseDto> syncNow() {
        return ResponseEntity.ok(syncService.syncInstruments());
    }
}