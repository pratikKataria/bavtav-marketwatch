package com.goldapp.psoni.controller;

import com.goldapp.psoni.dto.AddWatchlistRequest;
import com.goldapp.psoni.dto.TickData;
import com.goldapp.psoni.dto.WatchlistItemDto;
import com.goldapp.psoni.service.KiteTickerService;
import com.goldapp.psoni.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final KiteTickerService kiteTickerService;

    @PostMapping
    public ResponseEntity<Void> addSymbol(
            @RequestHeader("userId") Long userId,
            @RequestBody AddWatchlistRequest request) {

        watchlistService.addSymbol(userId, request.getInstrumentId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{instrumentId}")
    public ResponseEntity<Void> removeSymbol(
            @RequestHeader("userId") Long userId,
            @PathVariable Long instrumentId) {

        watchlistService.removeSymbol(userId, instrumentId);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<TickData>> getWatchlist(@RequestHeader("userId") Long userId) {
        try {
            return ResponseEntity.ok(watchlistService.getWatchlist(userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/default")
    public ResponseEntity<List<TickData>> getWatchlist() {
        try {
            return ResponseEntity.ok(watchlistService.getDefaultWatchList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Called when user adds or removes symbols from their watchlist.
     * Send the FULL current set of tokens (not a diff).
     * <p>
     * PUT /api/watchlist/{userId}
     * Body: [738561, 408065, 884737]
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateWatchlist(
            @PathVariable String userId,
            @RequestBody Set<Long> instrumentTokens) {

        kiteTickerService.updateWatchlist(userId, instrumentTokens);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns last known prices for a user's watchlist.
     * Call this on page load so the table isn't empty while waiting for ticks.
     * <p>
     * GET /api/watchlist/{userId}/snapshot
     */
    @GetMapping("/{userId}/snapshot")
    public ResponseEntity<List<TickData>> snapshot(@PathVariable Long userId) {
        return ResponseEntity.ok(kiteTickerService.getSnapshotForUser(userId));
    }

}