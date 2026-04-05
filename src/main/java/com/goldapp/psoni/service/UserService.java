package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.UpdateUserRequest;
import com.goldapp.psoni.dto.UserResponse;
import com.goldapp.psoni.entity.User;
import com.goldapp.psoni.mapper.UserMapper;
import com.goldapp.psoni.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserMapper.toResponse(user);
    }

    public UserResponse updateUser(Long userId, UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());

        user.setAddressLine1(request.getAddressLine1());
        user.setAddressLine2(request.getAddressLine2());

        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPincode(request.getPincode());
        user.setCountry(request.getCountry());

        user.setProfileImageUrl(request.getProfileImageUrl());

        userRepository.save(user);

        return UserMapper.toResponse(user);
    }
}