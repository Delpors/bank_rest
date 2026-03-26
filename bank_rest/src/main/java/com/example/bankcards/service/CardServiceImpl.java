package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UnauthorizedActionException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService{
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CardMapper cardMapper;

    @Transactional
    public CardResponse createCard(CardRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(()-> new UsernameNotFoundException
                        (String.format
                                ("Пользователь с id %s не найден", request.userId())));


        Card savedCard = cardRepository.save(
                new Card(
                request.cardNumber(),
                request.cardHolderName(),
                request.expireDate(),
                request.status(),
                request.balance(),
                null,
                request.blockedReason(),
                user
        ));

        return cardMapper.toCardResponse(savedCard);
    }

    @Transactional
    public CardResponse blockCard(Long cardId, String blockReason) {

        log.info("Попытка блокировки карты {}. Причина {}.", cardId, blockReason);
        return cardRepository.findById(cardId)
                .map(card -> {
                    card.block(blockReason);
                    return cardRepository.save(card);
                })
                .map(cardMapper::toCardResponse)
                .orElseThrow(() -> new CardNotFoundException(String.format("Карта с id %d не найдена", cardId)));
    }

    @Transactional
    public CardResponse activateCard(Long cardId) {

        return cardRepository.findById(cardId)
                .map(card -> {
                    card.activate();
                    return cardRepository.save(card);
                })
                .map(cardMapper::toCardResponse)
                .orElseThrow(() -> new CardNotFoundException(String.format("Карта с id %d не найдена", cardId)));
    }

    @Transactional
    public void deleteCard(Long cardId) {

        Card existingCard = cardRepository
                .findById(cardId)
                .orElseThrow(()->new CardNotFoundException(String.format("Карта с id %d не найдена!", cardId)));

        if (existingCard.isDeleted()){
            log.debug("Карта с id {} была удалена ранее", cardId);
            return;
        }

        log.info("Карта с id {} удалена!",cardId);
        existingCard.setDeleted(true);

    }

    public Page<CardResponse> getAllCards(String search, Pageable pageable) {

        Page<Card> cards = cardRepository.searchCards(search, pageable);
        return cards.map(cardMapper::toCardResponse);
    }

    public Page<CardResponse> getAllUsersCards(Long userId, String search, Pageable pageable) {

        Page<Card> cards;

        if (userRepository.existsById(userId)) {
            if (!StringUtils.hasText(search)) {
                cards = cardRepository.findAllByUserId(userId, pageable);
            } else {
                cards = cardRepository.findAllByUserId(userId, search, pageable);
            }
            return cards.map(cardMapper::toCardResponse);
        }else {
            throw new CardNotFoundException(String.format("Пользователь с id %d не найден", userId));
        }
    }

    public BigDecimal getTotalBalance(Long userid) {

        return cardRepository.findByUserId(userid)
                .stream()
                .map(Card::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public CardResponse blockUserCard(Long userId, Long cardId, String blocReason) {
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
        return cardMapper.toCardResponse(blockedCard);
    }

    @Transactional
    public TransactionResponse transferBetweenCards(Long userId, TransactionRequest request) {

        log.info("Попытка перевода средств с карты {} на карту {}", request.fromCardId(), request.toCardId());

        try {
            Card fromCard = cardRepository.findById(request.fromCardId())
                    .orElseThrow(()-> new CardNotFoundException(String.format("Карта с id %d не найдена", request.fromCardId())));
            Card toCard = cardRepository.findById(request.toCardId())
                    .orElseThrow(()-> new CardNotFoundException(String.format("Карта с id %d не найдена", request.toCardId())));


            if (fromCard.getBalance().compareTo(request.amount()) < 0) {
                throw new InsufficientFundsException(fromCard.getId(), fromCard.getBalance(), request.amount());
            }


            if (!fromCard.getUser().getId().equals(toCard.getUser().getId())){
                throw new AccessDeniedException("Карты не принадлежат одному пользователю");
            }

            BigDecimal newFromCard = fromCard.getBalance().subtract(request.amount());
            BigDecimal newToCard = toCard.getBalance().add(request.amount());

            fromCard.setBalance(newFromCard);
            toCard.setBalance(newToCard);

            return createTransaction(fromCard, toCard, request);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public TransactionResponse createTransaction(Card fromCard, Card toCard, TransactionRequest request){

        Transaction transaction = new Transaction();
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(request.amount());

        return TransactionResponse.toResponse(transactionRepository.save(transaction));
    }

    private String generateTransactionNumber() {
        return "TRN" + UUID.randomUUID().toString();
    }

}



