package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.AuthResponse;
import com.goldapp.psoni.dto.RequestOtpRequest;
import com.goldapp.psoni.dto.VerifyOtpRequest;
import com.goldapp.psoni.entity.EmailOtp;
import com.goldapp.psoni.entity.User;
import com.goldapp.psoni.enums.AuthProvider;
import com.goldapp.psoni.enums.OtpPurpose;
import com.goldapp.psoni.mapper.UserMapper;
import com.goldapp.psoni.repository.EmailOtpRepository;
import com.goldapp.psoni.repository.UserRepository;
import com.goldapp.psoni.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final int OTP_EXPIRY_MINUTES = 5;

    @Override
    public void requestOtp(RequestOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        String rawOtp = OtpUtil.generateOtp();

        EmailOtp emailOtp = EmailOtp.builder()
                .email(email)
                .otpHash(passwordEncoder.encode(rawOtp))
                .purpose(OtpPurpose.LOGIN)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .used(false)
                .attemptCount(0)
                .referenceId("EMAIL_LOGIN")
                .build();

        emailOtpRepository.save(emailOtp);

        emailService.sendOtpEmail(email, rawOtp);
    }

    @Override
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String email = normalizeEmail(request.getEmail());

        EmailOtp savedOtp = emailOtpRepository
                .findTopByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new RuntimeException("OTP not found or already used"));

        if (savedOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        if (!passwordEncoder.matches(request.getOtp(), savedOtp.getOtpHash())) {
            savedOtp.setAttemptCount(savedOtp.getAttemptCount() + 1);
            emailOtpRepository.save(savedOtp);
            throw new RuntimeException("Invalid OTP");
        }

        savedOtp.setUsed(true);
        emailOtpRepository.save(savedOtp);

        boolean isNewUser = false;

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .authProvider(AuthProvider.EMAIL_OTP)
                            .active(true)
                            .emailVerified(true)
                            .build();
                    return userRepository.save(newUser);
                });

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        isNewUser = user.getCreatedAt().equals(user.getUpdatedAt());

        // For now returning dummy token placeholder.
        // Later you can replace with JWT generation.
        String token = "TEMP_SESSION_TOKEN";

        return AuthResponse.builder()
                .message(isNewUser ? "Account created successfully" : "Login successful")
                .newUser(isNewUser)
                .user(userMapper.toDto(user))
                .token(token)
                .build();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}