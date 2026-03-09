package com.example.bankcards.dto;

import com.example.bankcards.entity.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        Boolean active,
        String userName,
        String fullName,
        String email,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
