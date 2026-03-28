package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.TestSecurityConfig;
import com.example.bankcards.service.CardService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(value = AdminCardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@Import(TestSecurityConfig.class)
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Autowired
    private ObjectMapper  objectMapper;

    private CardResponse cardResponse;
    private CardRequest cardRequest;

    @BeforeEach
    void setUp() {
         cardResponse = new CardResponse(
                "12345678912345678912",
                "Magomedov Magomed",
                LocalDate.now(),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(1000),
                LocalDateTime.now(),
                "d",
                1L,
                2L
        );

        cardRequest = new CardRequest(
                 "12345678912345678912",
                 "Magomedov Magomed",
                 LocalDate.now(),
                 CardStatus.ACTIVE,
                 BigDecimal.valueOf(1000),
                 "b",
                 2L
        );
    }

    @Nested
    @DisplayName("POST /api/admin/cards - Create Card")
    @WithMockUser(roles = "ADMIN")
    class CreateCardTests {

        @Test
        @DisplayName("Should create card successfully and return 201")
        void createCardSuccessfully()  throws Exception {
            when(cardService.createCard(any(CardRequest.class))).thenReturn(cardResponse);

            mockMvc.perform(post("/api/admin/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cardRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cardId").value(cardResponse.cardId()))
                    .andExpect(jsonPath("$.userId").value(cardResponse.userId()))
                    .andExpect(jsonPath("$.cardNumber").value(cardResponse.cardNumber()))
                    .andExpect(jsonPath("$.cardHolderName").value(cardResponse.cardHolderName()))
                    .andExpect(jsonPath("$.status").value(cardResponse.status().toString()))
                    .andExpect(jsonPath("$.balance").value(cardResponse.balance()))
                    .andExpect(jsonPath("$.blockedReason").value(cardResponse.blockedReason()));

            verify(cardService).createCard(any(CardRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request is invalid")
        void createCardInvalid()  throws Exception {
            CardRequest invalidCardRequest = new CardRequest(
                    "",
                    "INVALID",
                    null,
                    CardStatus.ACTIVE,
                    BigDecimal.valueOf(1000),
                    "b",
                    2L);

            mockMvc.perform(post("/api/admin/cards")
            .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidCardRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").exists());

            verify(cardService, never()).createCard(any(CardRequest.class));
        }
    }
}