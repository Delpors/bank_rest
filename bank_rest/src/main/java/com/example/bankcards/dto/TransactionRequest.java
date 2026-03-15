package com.example.bankcards.dto;

import java.math.BigDecimal;

public record TransactionRequest(
        Long fromCardId,
        Long toCardId,
        BigDecimal amount
) {
}
