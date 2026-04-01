package com.goldapp.psoni.service;

public interface EmailService {
    void sendOtpEmail(String email, String otp);
    void sendInstrumentSyncFailureEmail(Long syncLogId, int attemptNo, String errorMessage);
}