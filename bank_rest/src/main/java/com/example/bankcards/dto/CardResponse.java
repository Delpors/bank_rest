package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CardResponse(
        String cardNumber,
        String cardHolderName,
        LocalDateTime expireData,
        CardStatus status,
        BigDecimal balance,
        LocalDateTime blockedAt,
        String blockedReason,
        Long cardId,
        Long userId

) {
    public static List<CardResponse> fromListOfEntity(List<Card> cards){

        return cards.stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }
    public static CardResponse fromEntity(Card card){
        return new CardResponse(
                card.getCardNumber(),
                card.getCardHolderName(),
                card.getExpireData(),
                card.getStatus(),
                card.getBalance(),
                card.getBlockedAt(),
                card.getBlockReason(),
                card.getId(),
                card.getUser().getId()
        );
    }
}
