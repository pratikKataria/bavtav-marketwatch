package com.goldapp.psoni.controller;

import com.goldapp.psoni.dto.UpdateUserRequest;
import com.goldapp.psoni.dto.UserResponse;
import com.goldapp.psoni.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get User Information
     */
    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    /**
     * Update User Profile
     */
    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(userId, request);
    }
}