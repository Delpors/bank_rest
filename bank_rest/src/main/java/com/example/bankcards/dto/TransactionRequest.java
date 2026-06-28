package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull
        Long fromCardId,

        @NotNull
        Long toCardId,

        @Positive(message = "Amount must be positive")
        BigDecimal amount
) {
}
