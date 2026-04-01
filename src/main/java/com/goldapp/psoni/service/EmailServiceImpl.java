package com.goldapp.psoni.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${instrument.sync.alert.to}")
    private String alertTo;

    @Override
    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Market Watch OTP");
        message.setText("Your OTP for Market Watch login is: " + otp + ". It is valid for 5 minutes.");
        mailSender.send(message);
    }

    @Override
    public void sendInstrumentSyncFailureEmail(Long syncLogId, int attemptNo, String errorMessage) {

    }
}