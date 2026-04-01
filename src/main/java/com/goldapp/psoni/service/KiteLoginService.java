package com.goldapp.psoni.service;

import com.goldapp.psoni.entity.KiteSession;
import com.goldapp.psoni.repository.KiteSessionRepository;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Executes the full Zerodha 3-step login flow and persists the resulting
 * access token to the kite_session table.
 * <p>
 * Called by:
 * - KiteLoginScheduler  (7 AM daily, automated)
 * - AdminController     (on-demand via POST /api/admin/kite/relogin)
 */
@Service
public class KiteLoginService {

    private static final Logger log = LoggerFactory.getLogger(KiteLoginService.class);

    @Value("${zerodha.api-key}")
    private String apiKey;

    @Value("${zerodha.api-secret}")
    private String apiSecret;

    @Value("${zerodha.user-id}")
    private String userId;

    @Value("${zerodha.password}")
    private String password;

    @Value("${zerodha.totp-secret}")
    private String totpSecret;

    private final KiteSessionRepository sessionRepository;

    public KiteLoginService(KiteSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Runs the full login flow and stores the access token in the DB.
     * Deactivates any previous sessions before saving the new one.
     *
     * @return the newly created KiteSession
     */
    @Transactional
    public KiteSession loginAndPersist() throws KiteException, IOException {
        log.info("Starting Kite login flow for date: {}", LocalDate.now());

        // ── Cookie jar scoped to this login attempt ───────────────────────────
        Map<String, List<Cookie>> cookieStore = new HashMap<>();
        CookieJar cookieJar = buildCookieJar(cookieStore);

        OkHttpClient http = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .cookieJar(cookieJar)
                .build();

        // Step 1 — Password login
        String requestId = doPasswordLogin(http);

        // Step 2 — TOTP 2FA
        doTwoFa(http, requestId);

        // Step 3 — Fetch request_token via redirect
        String enctoken = extractCookie(cookieStore, "kite.zerodha.com", "enctoken");
        String requestToken = fetchRequestToken(http, enctoken);

        // Step 4 — Exchange for access token
        KiteConnect kite = new KiteConnect(apiKey);
        User user = kite.generateSession(requestToken, apiSecret);
        log.info("Login successful for Kite user: {}", user.userName);

        // Step 5 — Persist to DB (deactivate old, save new)
        sessionRepository.deactivateAllSessions();
        KiteSession session = sessionRepository.save(new KiteSession(user.accessToken));
        log.info("Access token persisted to DB — session ID: {}", session.getId());

        return session;
    }

    // ── Login steps ───────────────────────────────────────────────────────────

    private String doPasswordLogin(OkHttpClient http) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("user_id", userId)
                .add("password", password)
                .build();

        try (Response resp = http.newCall(new Request.Builder()
                .url("https://kite.zerodha.com/api/login")
                .post(body).build()).execute()) {

            String json = resp.body().string();
            JSONObject obj = new JSONObject(json);
            if (!"success".equals(obj.optString("status"))) {
                throw new IOException("Password login failed: " + json);
            }
            String requestId = obj.getJSONObject("data").getString("request_id");
            log.debug("Password login OK, request_id: {}", requestId);
            return requestId;
        }
    }

    private void doTwoFa(OkHttpClient http, String requestId) throws IOException {
        String totp = generateTotp();
        log.debug("Generated TOTP for 2FA");

        RequestBody body = new FormBody.Builder()
                .add("user_id", userId)
                .add("request_id", requestId)
                .add("twofa_value", totp)
                .add("twofa_type", "totp")
                .add("skip_session", "true")
                .build();

        try (Response resp = http.newCall(new Request.Builder()
                .url("https://kite.zerodha.com/api/twofa")
                .post(body).build()).execute()) {

            String json = resp.body().string();
            JSONObject obj = new JSONObject(json);
            if (!"success".equals(obj.optString("status"))) {
                throw new IOException("2FA failed: " + json);
            }
            log.debug("2FA OK");
        }
    }

    private String fetchRequestToken(OkHttpClient http, String enctoken) throws IOException {
        String loginUrl = new KiteConnect(apiKey).getLoginURL();

        try (Response resp = http.newCall(new Request.Builder()
                .url(loginUrl)
                .header("Cookie", "enctoken=" + enctoken)
                .get().build()).execute()) {

            String finalUrl = resp.request().url().toString();
            if (!finalUrl.contains("request_token")) {
                throw new IOException("request_token missing in redirect: " + finalUrl);
            }
            return finalUrl.split("request_token=")[1].split("&")[0];
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateTotp() {
        try {
            SystemTimeProvider time = new SystemTimeProvider();
            DefaultCodeGenerator gen = new DefaultCodeGenerator();
            return gen.generate(totpSecret, time.getTime() / 30);
        } catch (Exception e) {
            throw new RuntimeException("TOTP generation failed", e);
        }
    }

    private CookieJar buildCookieJar(Map<String, List<Cookie>> store) {
        return new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                Map<String, Cookie> merged = new LinkedHashMap<>();
                store.getOrDefault(url.host(), List.of()).forEach(c -> merged.put(c.name(), c));
                cookies.forEach(c -> merged.put(c.name(), c));
                store.put(url.host(), new ArrayList<>(merged.values()));
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                return store.getOrDefault(url.host(), new ArrayList<>());
            }
        };
    }

    private String extractCookie(Map<String, List<Cookie>> store, String host, String name) {
        return store.getOrDefault(host, List.of()).stream()
                .filter(c -> c.name().equals(name))
                .map(Cookie::value)
                .findFirst()
                .orElse("");
    }
}