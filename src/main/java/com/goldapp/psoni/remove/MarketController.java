//package com.goldapp.psoni;
//
//import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/market")
//public class MarketController {
//
//    @Autowired
//    private KiteService kiteService;
//
//    @GetMapping("/price/{exchange}/{symbol}")
//    public double getPrice(@PathVariable String exchange, @PathVariable String symbol) throws Exception, KiteException {
//        return kiteService.getLtp(exchange, symbol);
//    }
////
////    @GetMapping("/fno-price/{symbol}")
////    public double getFnOPrice(@PathVariable String symbol) throws Exception, KiteException {
////        return kiteService.getFnOLtp(symbol);
////    }
////
////    @GetMapping("/mcx-price/{symbol}")
////    public double getMcxPrice(@PathVariable String symbol) throws Exception, KiteException {
////        return kiteService.getMCX(symbol);
////    }
//}