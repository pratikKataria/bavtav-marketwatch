package com.goldapp.psoni.utils;

public final class InstrumentCsvUtil {

    private InstrumentCsvUtil() {
    }

    public static String safeValue(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        if (cleaned.isEmpty()) {
            return null;
        }

        if (cleaned.length() >= 2 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }

        return cleaned.isEmpty() ? null : cleaned;
    }
}