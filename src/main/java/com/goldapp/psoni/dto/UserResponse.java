package com.goldapp.psoni.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;

    private String phone;
    private String gender;
    private LocalDate dateOfBirth;

    private String addressLine1;
    private String addressLine2;

    private String city;
    private String state;
    private String pincode;
    private String country;

    private String profileImageUrl;

    private Boolean emailVerified;
    private Boolean active;

    private LocalDateTime createdAt;
}