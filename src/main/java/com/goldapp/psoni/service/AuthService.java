package com.goldapp.psoni.service;


import com.goldapp.psoni.dto.AuthResponse;
import com.goldapp.psoni.dto.RequestOtpRequest;
import com.goldapp.psoni.dto.VerifyOtpRequest;

public interface AuthService {
    void requestOtp(RequestOtpRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
}