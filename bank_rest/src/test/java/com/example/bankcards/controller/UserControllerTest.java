package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.dto.TransactionResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.TestSecurityConfig;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.ICardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICardService cardService;

    private UserPrincipal userPrincipal;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(USER_ID)
                .userName("testuser")
                .password("password")
                .rol(UserRole.USER)
                .active(true)
                .build();
        userPrincipal = new UserPrincipal(user);
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyCards_shouldReturnPageOfCardResponses() throws Exception {

        String search = "visa";
        Pageable pageable = PageRequest.of(0, 10);
        CardResponse cardResponse = new CardResponse(
                "4111111111111111",
                "John Doe",
                LocalDate.now().plusYears(3),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(1000.50),
                null,
                null,
                100L,
                USER_ID
        );
        Page<CardResponse> page = new PageImpl<>(List.of(cardResponse), pageable, 1);
        when(cardService.getAllUsersCards(eq(USER_ID), eq(search), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/user/cards")
                        .param("search", search)
                        .param("page", "0")
                        .param("size", "10")
                        .with(SecurityMockMvcRequestPostProcessors.user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.content[0].cardHolderName").value("John Doe"))
                .andExpect(jsonPath("$.content[0].balance").value(1000.50))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(cardService).getAllUsersCards(USER_ID, search, pageable);
    }

    @Test
    void blockUserCard_shouldReturnBlockedCardResponse() throws Exception {

        Long cardId = 200L;
        String blockedReason = "Lost card";
        CardRequest request = new CardRequest(null, null, null, null, null, blockedReason, null);

        CardResponse blockedCard = new CardResponse(
                "4111111111111111",
                "John Doe",
                LocalDate.now().plusYears(3),
                CardStatus.BLOCKED,
                BigDecimal.valueOf(1000.50),
                LocalDateTime.now(),
                blockedReason,
                cardId,
                USER_ID
        );
        when(cardService.blockUserCard(eq(USER_ID), eq(cardId), eq(blockedReason))).thenReturn(blockedCard);

        mockMvc.perform(post("/api/user/cards/{cardId}/block", cardId)
                        .param("blockedReason", blockedReason)
                        .with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
                        .with(csrf()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.blockedReason").value(blockedReason));

        verify(cardService).blockUserCard(USER_ID, cardId, blockedReason);
    }

    @Test
    void getTotalBalance_shouldReturnTotalBalance() throws Exception {

        BigDecimal totalBalance = BigDecimal.valueOf(5000.75);
        when(cardService.getTotalBalance(USER_ID)).thenReturn(totalBalance);

        mockMvc.perform(get("/api/user/cards/balance")
                        .with(SecurityMockMvcRequestPostProcessors.user(userPrincipal)))
                .andExpect(status().isOk())
                .andExpect(content().string("5000.75"));

        verify(cardService).getTotalBalance(USER_ID);
    }

    @Test
    void transferBetweenCards_shouldReturnTransactionResponse() throws Exception {

        TransactionRequest request = new TransactionRequest(100L, 200L, BigDecimal.valueOf(150.00));
        TransactionResponse dummyResponse = new TransactionResponse(15L, "654646","125212","52224",BigDecimal.valueOf(150.00),LocalDateTime.now());

        when(cardService.transferBetweenCards(eq(USER_ID), any(TransactionRequest.class))).thenReturn(dummyResponse);

        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15L))
                .andExpect(jsonPath("$.amount").value(150.00));

        verify(cardService).transferBetweenCards(eq(USER_ID), any(TransactionRequest.class));
    }

    @Test
    void transfer_shouldReturnBadRequestWhenAmountIsNegative() throws Exception {

        TransactionRequest invalidRequest = new TransactionRequest(100L, 200L, BigDecimal.valueOf(-10));
        when(cardService.transferBetweenCards(eq(USER_ID), any(TransactionRequest.class)))
                .thenThrow(new IllegalArgumentException("Amount must be positive"));

        mockMvc.perform(post("/api/user/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}