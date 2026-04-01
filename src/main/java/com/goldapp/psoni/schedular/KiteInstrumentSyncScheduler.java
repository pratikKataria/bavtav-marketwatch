package com.goldapp.psoni.schedular;

import com.goldapp.psoni.service.KiteInstrumentSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KiteInstrumentSyncScheduler {

    private final KiteInstrumentSyncService syncService;

    @Scheduled(cron = "${instrument.sync.cron:0 0 5 * * ?}", zone = "${app.timezone:Asia/Kolkata}")
    public void syncInstrumentsDaily() {
        log.info("Starting scheduled Kite instrument sync");
        syncService.syncInstruments();
        log.info("Completed scheduled Kite instrument sync");
    }
}