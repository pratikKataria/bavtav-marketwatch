package com.goldapp.psoni.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private Boolean emailVerified;
}