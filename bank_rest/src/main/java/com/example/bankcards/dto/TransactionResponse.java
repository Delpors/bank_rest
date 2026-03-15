package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String transactionNumber,
        String fromCard,
        String toCard,
        BigDecimal amount,
        LocalDateTime createdAt
) {
    public static String getCardMask(String cardNumber){
        return "**** **** **** " + cardNumber
                .substring(cardNumber.length()-4);
    }

    public static TransactionResponse toResponse(Transaction transaction){
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionNumber(),
                getCardMask(transaction.getFromCard().getCardNumber()),
                getCardMask(transaction.getToCard().getCardNumber()),
                transaction.getAmount(),
                transaction.getCreatedAt()
        );


    }
}
