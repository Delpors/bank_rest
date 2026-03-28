package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CardRequest(
        @NotBlank(message = "Card number is required")
        String cardNumber,

        @NotBlank(message = "Cardholder name in required")
        String cardHolderName,

        @NotNull(message = "Expire date is required")
        LocalDate expireDate,

        @NotNull(message = "Status is required")
        CardStatus status,

        @NotNull(message = "Balance is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Balance must be non-negative")
        BigDecimal balance,

        String blockedReason,

        @NotNull(message = "User id is required")
        @Positive(message = "User id must be positive")
        Long userId
) {
}
