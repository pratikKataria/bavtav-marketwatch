package com.goldapp.psoni.mapper;

import com.goldapp.psoni.dto.UserResponse;
import com.goldapp.psoni.dto.UserResponseDto;
import com.goldapp.psoni.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    public static UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .addressLine1(user.getAddressLine1())
                .addressLine2(user.getAddressLine2())
                .city(user.getCity())
                .state(user.getState())
                .pincode(user.getPincode())
                .country(user.getCountry())
                .profileImageUrl(user.getProfileImageUrl())
                .emailVerified(user.getEmailVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}