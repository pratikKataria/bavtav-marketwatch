package com.goldapp.psoni.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {

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

}