package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.dto.TransactionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CardService {

    CardResponse createCard(@Valid CardRequest request);

    CardResponse blockCard(@NotNull Long cardId, String blockReason);

    CardResponse activateCard(@NotNull Long cardId);

    void deleteCard(@NotNull Long cardId);

    Page<CardResponse> getAllCards(String search, Pageable pageable);

    Page<CardResponse> getAllUsersCards(@NotNull Long userId, String search, Pageable pageable);

    BigDecimal getTotalBalance(@NotNull Long userid);

    CardResponse blockUserCard(@NotNull Long userId, @NotNull Long cardId, String blocReason);

    TransactionResponse transferBetweenCards(@NotNull Long userId, @Valid TransactionRequest request);

}
