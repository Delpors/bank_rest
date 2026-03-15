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
    public static List<CardResponse> fromListOfEntity(List<Card> cards){

        return cards.stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public static String getCardMask(Card card){
        return "**** **** **** " + card.getCardNumber()
                .substring(card.getCardNumber().length()-4);
    }

    public static CardResponse fromEntity(Card card){

        return new CardResponse(
                getCardMask(card),
                card.getCardHolderName(),
                card.getExpireDate(),
                card.getStatus(),
                card.getBalance(),
                card.getBlockedAt(),
                card.getBlockReason(),
                card.getId(),
                card.getUser().getId()
        );
    }
}
