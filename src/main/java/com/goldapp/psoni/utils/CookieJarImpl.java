
package com.goldapp.psoni.utils;

import okhttp3.*;

import java.util.*;

public class CookieJarImpl implements CookieJar {

    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return cookieStore.getOrDefault(url.host(), new ArrayList<>());
    }
}