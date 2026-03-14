package com.example.bankcards.dto;

public record AuthResponse(
            String token,
            String userName,
            String fullName,
            String role
) {
}
