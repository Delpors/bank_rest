package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CardRequest(
    String cardNumber,
    String cardHolderName,
    LocalDate expireDate,
    CardStatus status,
    BigDecimal balance,
    String blockedReason,
    Long userId
) {
}
