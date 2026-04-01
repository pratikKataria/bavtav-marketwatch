package com.goldapp.psoni.remove;

//public class ZerodhaAutoLogin2 {
//
//    private static final String API_KEY = "vjm3aurw1s61dl3f";
//    private static final String API_SECRET = "cuqflrq3pqp3uw5bwuficg8uqetoztu5";
//    private static final String USER_ID = "IVW976";
//    private static final String PASSWORD = "Trading@123";
//    private static final String TOTP_SECRET = "6XFTLJ6P2HFLILCQQJCMQA5SV5EHGOQX";
//
//    public static void main(String[] args) throws Exception, KiteException {
//
//        // Replace your inline CookieJar with this
//        final Map<String, List<Cookie>> cookieStore = new HashMap<>();
//        CookieJar cookieJar = new CookieJar() {
//            @Override
//            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//                // Save under host, merging with existing cookies
//                List<Cookie> existing = cookieStore.getOrDefault(url.host(), new ArrayList<>());
//                Map<String, Cookie> merged = new HashMap<>();
//                for (Cookie c : existing) merged.put(c.name(), c);
//                for (Cookie c : cookies) merged.put(c.name(), c);  // new ones override
//                cookieStore.put(url.host(), new ArrayList<>(merged.values()));
//                System.out.println("Saved cookies for " + url.host() + ": " + merged.keySet());
//            }
//
//            @Override
//            public List<Cookie> loadForRequest(HttpUrl url) {
//                List<Cookie> cookies = cookieStore.getOrDefault(url.host(), new ArrayList<>());
//                System.out.println("Loading cookies for " + url.host() + ": " + cookies.size());
//                return cookies;
//            }
//        };
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .followRedirects(true)        // same as allow_redirects=True
//                .followSslRedirects(true)
//                .cookieJar(cookieJar)
//                .build();
//
//        System.out.println("Step 1 : Login");
//
//        RequestBody loginBody = new FormBody.Builder()
//                .add("user_id", USER_ID)
//                .add("password", PASSWORD)
//                .build();
//
//        Request loginRequest = new Request.Builder()
//                .url("https://kite.zerodha.com/api/login")
//                .post(loginBody)
//                .build();
//
//        Response loginResponse = client.newCall(loginRequest).execute();
//        String loginJson = loginResponse.body().string();
//
//        System.out.println("Login Response: " + loginJson);
//
//        JSONObject loginObj = new JSONObject(loginJson);
//
//        String requestId = loginObj.getJSONObject("data").getString("request_id");
//
//        System.out.println("Request ID: " + requestId);
//
//        System.out.println("Step 2 : Generate TOTP");
//
//        TimeProvider timeProvider = new SystemTimeProvider();
//        CodeGenerator codeGenerator = new DefaultCodeGenerator();
//
//        String totp = codeGenerator.generate(TOTP_SECRET, timeProvider.getTime() / 30);
//
//
//        System.out.println("TOTP: " + totp);
//
//        RequestBody twofaBody = new FormBody.Builder()
//                .add("user_id", USER_ID)
//                .add("request_id", requestId)
//                .add("twofa_value", String.valueOf(totp))
//                .add("twofa_type", "totp")
//                .add("skip_session", String.valueOf(true))
//                .build();
//
//        Request twofaRequest = new Request.Builder()
//                .url("https://kite.zerodha.com/api/twofa")
//                .post(twofaBody)
//                .build();
//
//        Response twofaResponse = client.newCall(twofaRequest).execute();
//
//        System.out.println("2FA Response: " + twofaResponse.body().string());
//
//        System.out.println("=== Cookies after 2FA ===");
//        cookieStore.forEach((host, cookies) -> {
//            System.out.println("Host: " + host);
//            cookies.forEach(c -> System.out.println("  " + c.name() + " = " + c.value()));
//        });
//
//        System.out.println("Step 3 : Get request token");
//
//        KiteConnect kite = new KiteConnect(API_KEY);
//        String loginUrl = kite.getLoginURL();
//        System.out.println("Login URL: " + loginUrl);
//
//        // Build request WITH explicit cookie header from zerodha session
//        // First, get the enctoken/session cookie value from your cookie jar
//        String enctoken = "";
//        for (Cookie c : cookieStore.getOrDefault("kite.zerodha.com", new ArrayList<>())) {
//            if (c.name().equals("enctoken") || c.name().equals("session")) {
//                enctoken = c.value();
//                System.out.println("Found session cookie: " + c.name() + "=" + enctoken);
//            }
//        }
//
//        Request request = new Request.Builder()
//                .url(loginUrl)
//                .header("Cookie", "enctoken=" + enctoken)  // explicitly pass cookie
//                .get()
//                .build();
//
//        Response response = client.newCall(request).execute();
//
//        String finalUrl = response.request().url().toString();
//        System.out.println("Final Redirect URL: " + finalUrl);
//
//        String requestToken = "";
//        if (finalUrl.contains("request_token")) {
//            requestToken = finalUrl.split("request_token=")[1].split("&")[0];
//            System.out.println("Request Token: " + requestToken);
//        } else {
//            // Try reading the response body for clues
//            System.out.println("Response code: " + response.code());
//            System.out.println("Response body: " + response.body().string());
//        }
//
//        System.out.println("Step 4 : Generate Access Token");
//
//        User user = kite.generateSession(requestToken, API_SECRET);
//
//        kite.setAccessToken(user.accessToken);
//
//        System.out.println("Access Token: " + user.accessToken);
//
//        System.out.println("User Profile: " + kite.getProfile().userName);
//    }
//}
//
