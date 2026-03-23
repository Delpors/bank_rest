package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CardResponse(
        String cardNumber,
        String cardHolderName,
        LocalDate expireDate,
        CardStatus status,
        BigDecimal balance,
        LocalDateTime blockedAt,
        String blockedReason,
        Long cardId,
        Long userId

) {
}
