package com.example.bankcards.dto;

public record RegisterRequest(
        String userName,
        String password,
        String fullName,
        String email
) {
}
