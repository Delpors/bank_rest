package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UnauthorizedActionException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public CardResponse createCard(@Valid CardRequest request) {

        Card savedCard = cardRepository.save(CardRequest.toEntity(request));
        return CardResponse.fromEntity(savedCard);
    }

    @Transactional
    public CardResponse blockCard(@NotNull Long cardId, String blockReason) {

        log.info("Попытка блокировки карты {}. Причина {}.", cardId, blockReason);
        return cardRepository.findById(cardId)
                .map(card -> {
                    card.block(blockReason);
                    return cardRepository.save(card);
                })
                .map(CardResponse::fromEntity)
                .orElseThrow(() -> new CardNotFoundException(String.format("Карта с id %d не найдена", cardId)));
    }

    @Transactional
    public CardResponse activateCard(@NotNull Long cardId) {

        return cardRepository.findById(cardId)
                .map(card -> {
                    card.activate();
                    return cardRepository.save(card);
                })
                .map(CardResponse::fromEntity)
                .orElseThrow(() -> new CardNotFoundException(String.format("Карта с id %d не найдена", cardId)));
    }

    public void deleteCard(@NotNull Long cardId) {

        try {
            cardRepository.findById(cardId);
        } catch (CardNotFoundException e) {
            throw new CardNotFoundException(String.format("Карта с id %d не найдена", cardId));
        }
        cardRepository.deleteById(cardId);

    }

    public Page<CardResponse> getAllCards(String search, Pageable pageable) {

        Page<Card> cards = cardRepository.searchCards(search, pageable);
        return cards.map(CardResponse::fromEntity);
    }

    public Page<CardResponse> getAllUsersCards(@NotNull Long userId, String search, Pageable pageable) {

        Page<Card> cards;

        if (userRepository.existsById(userId)) {
            if (!StringUtils.hasText(search)) {
                cards = cardRepository.findAllByUserId(userId, pageable);
            } else {
                cards = cardRepository.findAllByUserId(userId, search, pageable);
            }
            return cards.map(CardResponse::fromEntity);
        }else {
            throw new CardNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
    }

    public BigDecimal getTotalBalance(@NotNull Long userid) {

        return cardRepository.findByUserId(userid)
                .stream()
                .map(Card::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public CardResponse blockUserCard(@NotNull Long userId, Long cardId, String blocReason) {
        log.info("Пользователь {} запрашивает блокировку карты {}",userId,cardId);

        userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(String.format("Пользователь с id %d не найден", userId)));
        Card card = cardRepository.findById(cardId)
                .orElseThrow(()-> new CardNotFoundException(String.format("Карта с id %d не найдена", cardId)));

        if (!Objects.equals(card.getUser().getId(),userId)){
            log.warn("Попытка блокировки чужой карты. Пользователь {}, карта принадлежит {}.",userId, card.getUser().getId());
            throw new UnauthorizedActionException("Вы не можете блокировать чужую карту.");
        }

        card.block(blocReason);
        Card blockedCard = cardRepository.save(card);
        return CardResponse.fromEntity(blockedCard);
    }

    @Transactional
    public TransactionResponse transferBetweenCards(@NotNull Long userId, @Valid TransactionRequest request) {

        log.info("Попытка перевода средств с карты {} на карту {}", request.fromcard(), request.toCard());

        try {

            if (request.fromcard().getBalance().compareTo(request.amount()) < 0) {
                log.warn("На карте {}, недостаточно средств, на балансе: {} рублей, запрошено {}",
                        request.fromcard(), request.fromcard().getBalance(), request.amount());
            }

            cardRepository.findById(request.fromcard().getId())
                     .orElseThrow(()-> new CardNotFoundException(String.format("Карта с id %d не найдена", request.fromcard().getId())));
            cardRepository.findById(request.toCard().getId())
                    .orElseThrow(()-> new CardNotFoundException(String.format("Карта с id %d не найдена", request.toCard().getId())));

            if (!request.fromcard().getUser().getId().equals(request.toCard().getUser().getId())){
                throw new AccessDeniedException("Карты не принадлежат одному пользователю");
            }

            BigDecimal newFromCard = request.fromcard().getBalance().subtract(request.amount());
            BigDecimal newToCard = request.toCard().getBalance().add(request.amount());

            request.fromcard().setBalance(newFromCard);
            request.toCard().setBalance(newToCard);

            Transaction transaction = TransactionRequest.toEntity(request);
            transactionRepository.save(transaction);

            return TransactionResponse.toResponse(transaction);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



