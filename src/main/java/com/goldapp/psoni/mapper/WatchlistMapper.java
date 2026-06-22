//package com.goldapp.psoni.mapper;
//
//import com.goldapp.psoni.dto.TickData;
//import com.goldapp.psoni.dto.WatchlistItemDto;
//import com.goldapp.psoni.entity.InstrumentMaster;
//import com.goldapp.psoni.entity.UserWatchlist;
//
//import java.math.BigDecimal;
//
//public class WatchlistMapper {
//
//    private WatchlistMapper() {}
//
//    /** Step 1: Build DTO from DB entities (no tick data yet) */
//    public static WatchlistItemDto fromEntities(UserWatchlist uw, InstrumentMaster im) {
//        return WatchlistItemDto.builder()
//                .watchlistId(uw.getId())
//                .displayOrder(uw.getDisplayOrder())
//                // InstrumentMaster fields
//                .instrumentId(im.getId())
//                .instrumentToken(im.getInstrumentToken())
//                .exchangeToken(im.getExchangeToken())
//                .exchange(im.getExchange())
//                .symbol(im.getSymbol())
//                .name(im.getName())
//                .instrumentType(im.getInstrumentType())
//                .segment(im.getSegment())
//                .expiryDate(im.getExpiryDate())
//                .strikePrice(im.getStrikePrice())
//                .tickSize(im.getTickSize())
//                .lotSize(im.getLotSize())
//                .build();
//    }
//
//    /** Step 2: Enrich DTO with live tick data from Kite */
//    public static void enrichWithTick(WatchlistItemDto dto, TickData tick) {
//        dto.setLastPrice(BigDecimal.valueOf(tick.getLastPrice()));
//        dto.setChange(BigDecimal.valueOf(tick.getChange()));
//        dto.setChangePercent(BigDecimal.valueOf(tick.getChangePercent()));
//        dto.setVolume((long) tick.getVolume());
//        dto.setOpen(BigDecimal.valueOf(tick.getOpen()));
//        dto.setHigh(BigDecimal.valueOf(tick.getHigh()));
//        dto.setLow(BigDecimal.valueOf(tick.getLow()));
//        dto.setClose(BigDecimal.valueOf(tick.getClose()));
////        dto.setBuyPrice(tick.getBuyPrice());
////        dto.setSellPrice(tick.getSellPrice());
//    }
//}