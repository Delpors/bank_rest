package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse createUser(UserRequest userRequest);
    UserResponse blockUser(Long userId);
    UserResponse activateUser(Long userId);
    void deleteUser(@NotNull Long userId);
    User getUserById(Long userId);
}
