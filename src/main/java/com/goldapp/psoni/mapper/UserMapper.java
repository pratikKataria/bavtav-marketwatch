package com.goldapp.psoni.mapper;

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
}