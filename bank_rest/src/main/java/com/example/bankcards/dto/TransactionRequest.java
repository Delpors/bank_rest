package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(
        String transactionNumber,
        Card fromcard,
        Card toCard,
        BigDecimal amount,
        LocalDateTime createdAt
) {
    public static Transaction toEntity(TransactionRequest request){
        return new Transaction(
                null,
                request.transactionNumber,
                request.fromcard,
                request.toCard,
                request.amount,
                request.createdAt
        );
    }
}
