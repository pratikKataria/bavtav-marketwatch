package com.goldapp.psoni.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String message;
    private Boolean newUser;
    private UserResponseDto user;
    private String token;
}