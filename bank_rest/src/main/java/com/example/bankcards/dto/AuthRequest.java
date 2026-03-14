package com.example.bankcards.dto;

public record AuthRequest(
        String userName,
        String password
) {
}
