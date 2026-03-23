package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardMapper {
    public List<CardResponse> toListOfCardsResponse(List<Card> cards){

        return cards
                .stream()
                .map(this::toCardResponse)
                .collect(Collectors.toList());
    }

    public CardResponse toCardResponse(Card card) {

        return new CardResponse(
                MaskingService.maskCardNumber(card.getCardNumber()),
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

    public Card toEntity(CardRequest cardRequest) {

        Card card = new Card();
        card.setCardNumber(cardRequest.cardNumber());
        card.setCardHolderName(cardRequest.cardHolderName());
        card.setExpireDate(cardRequest.expireDate());
        card.setStatus(cardRequest.status());
        card.setBalance(cardRequest.balance());
        card.setBlockReason(cardRequest.blockedReason());

        return card;
    }

}
