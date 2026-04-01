//package com.goldapp.psoni.service;
//
//import com.zerodhatech.kiteconnect.KiteConnect;
//import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
//import com.zerodhatech.models.User;
//import org.springframework.stereotype.Service;
//
//@Service
//public class KiteAuthService {
//
//    private final String apiKey = "vjm3aurw1s61dl3f";
//    private final String apiSecret = "cuqflrq3pqp3uw5bwuficg8uqetoztu5";
//    private final String requestToken = "z32BkFKEQ1FfrKI3SEjAsBErJ3DbfexG";
//
//    public User generateSession() throws Exception {
//
//        KiteConnect kiteConnect = new KiteConnect(apiKey);
//
//        User user = null;
//        try {
//            user = kiteConnect.generateSession(requestToken, apiSecret);
//        } catch (KiteException e) {
//            throw new RuntimeException(e);
//        }
//
//        kiteConnect.setAccessToken(user.accessToken);
//        kiteConnect.setPublicToken(user.publicToken);
//
//        return user;
//    }
//}
//
////{
////        "products": [
////        "CNC",
////        "NRML",
////        "MIS",
////        "BO",
////        "CO"
////        ],
////        "userName": "Parvesh Soni",
////        "broker": "ZERODHA",
////        "accessToken": "FMj3g56MoT6jsv2Omk3LLN1yG66nkrPj",
////        "publicToken": "E8n8WSMgB2fp12NdRm8mcW4OFQs7V9WD",
////        "userType": "individual/ind_with_nom",
////        "userId": "IVW976",
////        "loginTime": "2026-03-29T07:40:33.000+00:00",
////        "apiKey": "vjm3aurw1s61dl3f",
////        "exchanges": [
////        "NSE",
////        "MF",
////        "BSE"
////        ],
////        "orderTypes": [
////        "MARKET",
////        "LIMIT",
////        "SL",
////        "SL-M"
////        ],
////        "email": "aluniseas@gmail.com",
////        "refreshToken": "",
////        "shortName": "Parvesh",
////        "avatarURL": null
////        }