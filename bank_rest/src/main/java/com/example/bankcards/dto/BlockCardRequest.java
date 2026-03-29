package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;

public record BlockCardRequest (
        @NotBlank(message = "Block reason is required")
        String blockReason
){
}
