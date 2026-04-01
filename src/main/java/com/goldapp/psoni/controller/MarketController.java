package com.goldapp.psoni.controller;

import com.goldapp.psoni.dto.MarketInstrumentDto;
import com.goldapp.psoni.dto.TickData;
//import com.goldapp.psoni.service.KiteTickerService;
import com.goldapp.psoni.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;
//    private final KiteTickerService kiteTickerService;

    @GetMapping("/default")
    public ResponseEntity<List<MarketInstrumentDto>> getDefaultSymbols() throws Exception {

        return ResponseEntity.ok(
                marketService.getDefaultMarketSymbols()
        );
    }


    /**
     * Snapshot endpoint — call once on page load to populate UI immediately.
     * After this, switch to WebSocket for live updates.
     */
//    @GetMapping("/snapshot")
//    public ResponseEntity<Collection<TickData>> getSnapshot() {
//        Map<Long, TickData> allTicks = kiteTickerService.getAllLastTicks();
//        return ResponseEntity.ok(allTicks.values());
//    }

//    @GetMapping("/snapshot/{token}")
//    public ResponseEntity<TickData> getSnapshotForToken(@PathVariable long token) {
//        TickData tick = kiteTickerService.getLastTick(token);
//        if (tick == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(tick);
//    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Market watch service running");
    }
}