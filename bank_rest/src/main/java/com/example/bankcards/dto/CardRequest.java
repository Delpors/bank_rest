package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CardRequest(
    @NotBlank String cardNumber,
    @NotBlank String cardHolderName,
    @NotBlank LocalDateTime expireData,
    @NotBlank CardStatus status,
    @NotBlank BigDecimal balance,
    String blockedReason,
    @NotNull User user
) {
    public static Card toEntity(CardRequest request){

        return new Card(
                request.cardNumber,
                request.cardHolderName,
                request.expireData,
                request.status,
                request.balance,
                null,
                request.blockedReason,
                request.user
        );
    }
}
