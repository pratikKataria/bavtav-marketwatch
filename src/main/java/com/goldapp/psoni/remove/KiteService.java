//package com.goldapp.psoni;
//
//import com.zerodhatech.kiteconnect.KiteConnect;
//import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
//import com.zerodhatech.models.LTPQuote;
//import com.zerodhatech.models.Order;
//import com.zerodhatech.models.User;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class KiteService {
////    Access Token: Dekzb6IizrDlEOM2xkBNgU9II0YoESSY
////    Public Token: 0gEtkWvmOFa7KMYVdX5CLdF6ZFnU6HVo
////    Access Token: dMB7oxCIE3Cho4Q9eSuhpv6qTCH7xIKa
////    Public Token: 1DGkGGql7Xwr5eaWww6DJHp1a4pSl505
//
//    private final String apiKey = "vjm3aurw1s61dl3f";
//    private final String apiSecret = "cuqflrq3pqp3uw5bwuficg8uqetoztu5";
//    private final String requestToken = "GFrftpw68LOKvLWwNmMQ60jSHpEx8KYz";
//
//    public double getLtp(String symbol) throws Exception, KiteException {
////        https://kite.zerodha.com/connect/login?v=3&api_key=vjm3aurw1s61dl3f
////        https://developers.kite.trade/create/oneone?request_token=iVeiTeJG4o08tAl0f65VOP1FL6sLzolx&action=login&type=login&status=success
////        https://developers.kite.trade/create/oneone?action=login&type=login&status=success&request_token=GFrftpw68LOKvLWwNmMQ60jSHpEx8KYz
//        String apiKey = "vjm3aurw1s61dl3f";
//        String apiSecret = "cuqflrq3pqp3uw5bwuficg8uqetoztu5";
//        String requestToken = "GFrftpw68LOKvLWwNmMQ60jSHpEx8KYz";
//
//        KiteConnect kiteConnect = new KiteConnect("apiKey");
//
////        User user = kiteConnect.generateSession(requestToken, apiSecret);
//
//        String accessToken = /*user.accessToken*/ "dMB7oxCIE3Cho4Q9eSuhpv6qTCH7xIKa";
//        String publicToken = /*user.publicToken*/ "1DGkGGql7Xwr5eaWww6DJHp1a4pSl505";
//
//        System.out.println("Access Token: " + accessToken);
//        System.out.println("Public Token: " + publicToken);
//
//        kiteConnect.setAccessToken(accessToken);
//
//        String[] instruments = {"NSE:" + symbol};
//
//        List<Order> orders = kiteConnect.getOrders();
//        System.out.println("orders " + orders.toString());
//
//        Map<String, LTPQuote> ltp = kiteConnect.getLTP(instruments);
//
//        return ltp.get("NSE:" + symbol).lastPrice;
//    }
//
//    public double getFnOLtp(String tradingSymbol) throws Exception, KiteException {
//        String apiKey = "vjm3aurw1s61dl3f";
//        String accessToken = "dMB7oxCIE3Cho4Q9eSuhpv6qTCH7xIKa";
//
//        KiteConnect kiteConnect = new KiteConnect(apiKey);
//        kiteConnect.setAccessToken(accessToken);
//
//        String instrument = "NFO:" + tradingSymbol.toUpperCase(); // Ensure uppercase
//        Map<String, LTPQuote> ltp = kiteConnect.getLTP(new String[]{instrument});
//
//        if (ltp != null && ltp.containsKey(instrument)) {
//            return ltp.get(instrument).lastPrice;
//        } else {
//            throw new Exception("Instrument not found in response: " + instrument);
//        }
//    }
//
//    public double getMCX(String tradingSymbol) throws Exception, KiteException {
//        String apiKey = "vjm3aurw1s61dl3f";
//        String accessToken = "dMB7oxCIE3Cho4Q9eSuhpv6qTCH7xIKa";
//
//        KiteConnect kiteConnect = new KiteConnect(apiKey);
//        kiteConnect.setAccessToken(accessToken);
//
//        String instrument = "MCX:" + tradingSymbol.toUpperCase(); // Ensure uppercase
//        Map<String, LTPQuote> ltp = kiteConnect.getLTP(new String[]{instrument});
//
//        if (ltp != null && ltp.containsKey(instrument)) {
//            return ltp.get(instrument).lastPrice;
//        } else {
//            throw new Exception("Instrument not found in response: " + instrument);
//        }
//    }
//
//
//    public double getLtp(String exchange, String tradingSymbol) throws Exception, KiteException {
//
//        String apiKey = "vjm3aurw1s61dl3f";
//        String accessToken = "dMB7oxCIE3Cho4Q9eSuhpv6qTCH7xIKa";
//
//        KiteConnect kiteConnect = new KiteConnect(apiKey);
//        kiteConnect.setAccessToken(accessToken);
//
//        String instrument = exchange + ":" + tradingSymbol.toUpperCase();
//
//        Map<String, LTPQuote> ltp = kiteConnect.getLTP(new String[]{instrument});
//
//        if (ltp != null && ltp.containsKey(instrument)) {
//            return ltp.get(instrument).lastPrice;
//        }
//
//        throw new Exception("Instrument not found: " + instrument);
//    }
//
////
////    public double getLtp(String symbol) throws Exception, KiteException {
////        String apiKey = "";
////        String apiSecret = "";
////        String requestToken = "";
////
////        KiteConnect kiteConnect = new KiteConnect(apiKey);
////
//////        User user = kiteConnect.generateSession(requestToken, apiSecret);
////
////        String accessToken = /*user.accessToken*/ "";
////        String publicToken = /*user.publicToken*/ "";
////
////        System.out.println("Access Token: " + accessToken);
////        System.out.println("Public Token: " + publicToken);
////
////        kiteConnect.setAccessToken(accessToken);
////
////        String[] instruments = {"NSE:" + symbol};
////
////        List<Order> orders = kiteConnect.getOrders();
////        System.out.println("orders " + orders.toString());
////
////        Map<String, LTPQuote> ltp = kiteConnect.getLTP(instruments);
////
////        return ltp.get("NSE:" + symbol).lastPrice;
////    }
//}
//
//
