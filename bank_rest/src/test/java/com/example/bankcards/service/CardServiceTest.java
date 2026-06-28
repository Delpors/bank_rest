package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;
    private CardRequest cardRequest;
    private CardResponse cardResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        card = new Card();
        card.setId(1L);
        card.setCardNumber("1234567890123456");
        card.setCardHolderName("John Doe");
        card.setExpireDate(LocalDate.of(2025, 12, 31));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000.00));
        card.setUser(user);
        card.setDeleted(false);

        cardRequest = new CardRequest(
                "1234567890123456",
                "John Doe",
                LocalDate.of(2025, 12, 31),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(1000.00),
                null,
                1L
        );

        cardResponse = new CardResponse(
                "1234567890123456",
                "John Doe",
                LocalDate.of(2025, 12, 31),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(1000.00),
                null,
                null,
                1L,
                1L
        );

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createCard_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toCardResponse(any(Card.class))).thenReturn(cardResponse);

        CardResponse result = cardService.createCard(cardRequest);

        assertThat(result).isEqualTo(cardResponse);
        verify(userRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
        verify(cardMapper).toCardResponse(any(Card.class));
    }

    @Test
    void createCard_UserNotFound_ThrowsUsernameNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(cardRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Пользователь с id 1 не найден");
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockCard_Success() {
        String reason = "Fraud";
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toCardResponse(any(Card.class))).thenReturn(cardResponse);

        CardResponse result = cardService.blockCard(1L, reason);

        assertThat(result).isEqualTo(cardResponse);
        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(card.getBlockReason()).isEqualTo(reason);
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_CardNotFound_ThrowsCardNotFoundException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.blockCard(1L, "reason"))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Карта с id 1 не найдена");
        verify(cardRepository, never()).save(any());
    }

    @Test
    void activateCard_Success() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toCardResponse(any(Card.class))).thenReturn(cardResponse);

        CardResponse result = cardService.activateCard(1L);

        assertThat(result).isEqualTo(cardResponse);
        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        verify(cardRepository).findById(1L);
        verify(cardMapper).toCardResponse(card);
    }

    @Test
    void activateCard_CardNotFound_ThrowsCardNotFoundException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.activateCard(1L))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Карта с id 1 не найдена");
    }

    @Test
    void activateCard_AlreadyActive_ThrowsCardAlreadyActiveException() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.activateCard(1L))
                .isInstanceOf(CardAlreadyActiveException.class)
                .hasMessageContaining("она уже активирована");
        verify(cardRepository, never()).save(any());
    }

    @Test
    void deleteCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        assertThat(card.isDeleted()).isTrue();
        verify(cardRepository).findById(1L);
    }

    @Test
    void deleteCard_CardNotFound_ThrowsCardNotFoundException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(1L))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Карта с id 1 не найдена");
    }

    @Test
    void deleteCard_AlreadyDeleted_DoesNothing() {
        card.setDeleted(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        assertThat(card.isDeleted()).isTrue();
        verify(cardRepository).findById(1L);
    }

    @Test
    void getAllCards_WithSearch_ReturnsPage() {
        String search = "1234";
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.searchCards(search, pageable)).thenReturn(cardPage);
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);

        Page<CardResponse> result = cardService.getAllCards(search, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(cardResponse);
        verify(cardRepository).searchCards(search, pageable);
    }

    @Test
    void getAllCards_WithoutSearch_ReturnsPage() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(cardRepository.searchCards(null, pageable)).thenReturn(cardPage);
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);

        Page<CardResponse> result = cardService.getAllCards(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(cardRepository).searchCards(null, pageable);
    }

    @Test
    void getAllUsersCards_WithSearch_UserExists_ReturnsPage() {
        String search = "1234";
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.findAllByUserId(1L, search, pageable)).thenReturn(cardPage);
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);

        Page<CardResponse> result = cardService.getAllUsersCards(1L, search, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).existsById(1L);
        verify(cardRepository).findAllByUserId(1L, search, pageable);
    }

    @Test
    void getAllUsersCards_WithoutSearch_UserExists_ReturnsPage() {
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.findAllByUserId(1L, pageable)).thenReturn(cardPage);
        when(cardMapper.toCardResponse(card)).thenReturn(cardResponse);

        Page<CardResponse> result = cardService.getAllUsersCards(1L, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).existsById(1L);
        verify(cardRepository).findAllByUserId(1L, pageable);
    }

    @Test
    void getAllUsersCards_UserNotFound_ThrowsCardNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.getAllUsersCards(1L, null, pageable))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Пользователь с id 1 не найден");
        verify(cardRepository, never()).findAllByUserId(anyLong(), any(Pageable.class));
        verify(cardRepository, never()).findAllByUserId(anyLong(), anyString(), any(Pageable.class));
    }

    @Test
    void getTotalBalance_ReturnsSum() {
        Card card2 = new Card();
        card2.setBalance(BigDecimal.valueOf(500.00));
        when(cardRepository.findByUserId(1L)).thenReturn(List.of(card, card2));

        BigDecimal total = cardService.getTotalBalance(1L);

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(1500.00));
        verify(cardRepository).findByUserId(1L);
    }

    @Test
    void getTotalBalance_EmptyList_ReturnsZero() {
        when(cardRepository.findByUserId(1L)).thenReturn(List.of());

        BigDecimal total = cardService.getTotalBalance(1L);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
        verify(cardRepository).findByUserId(1L);
    }

    @Test
    void blockUserCard_Success() {
        String reason = "Suspicious";
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toCardResponse(any(Card.class))).thenReturn(cardResponse);

        CardResponse result = cardService.blockUserCard(1L, 1L, reason);

        assertThat(result).isEqualTo(cardResponse);
        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(card.getBlockReason()).isEqualTo(reason);
        verify(userRepository).findById(1L);
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(card);
    }

    @Test
    void blockUserCard_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.blockUserCard(1L, 1L, "reason"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с id 1 не найден");
        verify(cardRepository, never()).findById(anyLong());
    }

    @Test
    void blockUserCard_CardNotFound_ThrowsCardNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.blockUserCard(1L, 1L, "reason"))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Карта с id 1 не найдена");
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockUserCard_CardNotBelongToUser_ThrowsUnauthorizedActionException() {
        User otherUser = new User();
        otherUser.setId(2L);
        card.setUser(otherUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.blockUserCard(1L, 1L, "reason"))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("Вы не можете блокировать чужую карту");
        verify(cardRepository, never()).save(any());
    }

    @Test
    void transferBetweenCards_Success() throws AccessDeniedException {
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        fromCard.setUser(user);
        fromCard.setCardNumber("1111222233334444");

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setUser(user); // same user
        toCard.setCardNumber("5555666677778888");

        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.valueOf(200.00));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        TransactionResponse expectedResponse = new TransactionResponse(
                1L,
                "TRN" + UUID.randomUUID().toString(),
                "**** **** **** 4444",
                "**** **** **** 8888",
                BigDecimal.valueOf(200.00),
                LocalDateTime.now()
        );

        TransactionResponse result = cardService.transferBetweenCards(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        assertThat(result.fromCard()).isEqualTo("**** **** **** 4444");
        assertThat(result.toCard()).isEqualTo("**** **** **** 8888");
        assertThat(result.transactionNumber()).startsWith("TRN");

        assertThat(fromCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800.00));
        assertThat(toCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700.00));

        verify(cardRepository).findById(1L);
        verify(cardRepository).findById(2L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferBetweenCards_InsufficientFunds_ThrowsInsufficientFundsException() {
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(100.00));
        fromCard.setUser(user);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setUser(user);

        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.valueOf(200.00));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.transferBetweenCards(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Недостаточно средств")
                .hasMessageContaining("1")
                .hasMessageContaining("100")
                .hasMessageContaining("200");

        assertThat(fromCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(toCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferBetweenCards_FromCardNotFound_ThrowsCardNotFoundException() {
        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.valueOf(200));
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.transferBetweenCards(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Карта с id 1 не найдена");
    }

    @Test
    void transferBetweenCards_ToCardNotFound_ThrowsCardNotFoundException() {
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.valueOf(200));

        assertThatThrownBy(() -> cardService.transferBetweenCards(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Карта с id 2 не найдена");
    }

    @Test
    void transferBetweenCards_CardsDifferentUsers_ThrowsAccessDeniedException() {
        User otherUser = new User();
        otherUser.setId(2L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        fromCard.setUser(user);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setUser(otherUser); // different user

        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.valueOf(200));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.transferBetweenCards(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Карты не принадлежат одному пользователю");

        assertThat(fromCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(toCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_Success() {
        Card fromCard = new Card();
        fromCard.setCardNumber("1111222233334444");
        Card toCard = new Card();
        toCard.setCardNumber("5555666677778888");
        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.valueOf(100));

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionNumber("TRN123");
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCreatedAt(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = cardService.createTransaction(fromCard, toCard, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.transactionNumber()).isEqualTo("TRN123");
        assertThat(response.fromCard()).isEqualTo("**** **** **** 4444");
        assertThat(response.toCard()).isEqualTo("**** **** **** 8888");
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(100));

        verify(transactionRepository).save(any(Transaction.class));
    }
}
