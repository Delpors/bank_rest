package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockCardRequest;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardAlreadyActiveException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.TestSecurityConfig;
import com.example.bankcards.service.CardService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

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
    private ObjectMapper objectMapper;

    private CardResponse cardResponse;
    private CardRequest cardRequest;

    @BeforeEach
    void setUp() {
        cardResponse = new CardResponse(
                "12345678912345678912",
                "Рашидов Сабир",
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
                "Рашидов Сабир",
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
        @DisplayName("Должен успешно создать карту и вернуть 201")
        void createCardSuccessfully() throws Exception {
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
        @DisplayName("Должен вернуть 400 при неправильном запросе")
        void createCardInvalid() throws Exception {
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

        @ParameterizedTest
        @MethodSource("provideInvalidCardRequests")
        @DisplayName("Должен проверить реквизиты карты")
        void validateCardRequestFields(CardRequest request, String expectedFields) throws Exception {

            mockMvc.perform(post("/api/admin/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(cardService, never()).createCard(any());
        }

        private static Stream<Arguments> provideInvalidCardRequests() {

            return Stream.of(
                    Arguments.of(new CardRequest("", "INVALID", null, CardStatus.ACTIVE, BigDecimal.valueOf(1000), "b", 2L), "cardNumber"),
                    Arguments.of(new CardRequest("01234567890123456789", "", null, CardStatus.ACTIVE, BigDecimal.valueOf(1000), "b", 2L), "cardHolderName"),
                    Arguments.of(new CardRequest("01234558890123456789", "INVALID", null, CardStatus.ACTIVE, BigDecimal.valueOf(-5), "b", 2L), "balance")
            );
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/cards/{cardId}/block - Заблокировать карту")
    class blockCardTest {

        @Test
        @DisplayName("Должен заблокировать карту и вернуть 200")
        void blockCardSuccessfully() throws Exception {

            Long cardId = 1L;
            String blockedReason = "Истек срок";

            CardResponse blockedResponse = new CardResponse(
                    cardResponse.cardNumber(),
                    cardResponse.cardHolderName(),
                    cardResponse.expireDate(),
                    CardStatus.BLOCKED,
                    cardResponse.balance(),
                    LocalDateTime.now(),
                    cardResponse.blockedReason(),
                    cardResponse.cardId(),
                    cardResponse.userId()
            );

            BlockCardRequest blockCardRequest = new BlockCardRequest(blockedReason);

            when(cardService.blockCard(cardId, blockedReason)).thenReturn(blockedResponse);

            mockMvc.perform(put("/api/admin/cards/{cardId}/block", cardId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(blockCardRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cardId").value(cardResponse.cardId()))
                    .andExpect(jsonPath("$.status").value("BLOCKED"))
                    .andExpect(jsonPath("$.cardNumber").value(cardResponse.cardNumber()));

            verify(cardService, times(1)).blockCard(cardId, blockedReason);
        }

        @Test
        @DisplayName("Должен вернуть 400, если причина блокировки отсутствует")
        void blockCardInvalid() throws Exception {
            Long cardId = 1L;

            mockMvc.perform(put("/api/admin/cards/{cardId}/block", cardId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(null)))
                    .andExpect(status().isBadRequest());

            verify(cardService, never()).blockCard(any(), any());
        }

        @Test
        @DisplayName("Должен вернуть 404 если карта не найдена")
        void blockCardNotFound() throws Exception {
            Long cardId = 100L;
            BlockCardRequest blockCardRequest = new BlockCardRequest("Истек срок");

            when(cardService.blockCard(eq(cardId), anyString()))
                    .thenThrow(new CardNotFoundException("Карта не найдена. id " + cardId));

            mockMvc.perform(put("/api/admin/cards/{cardId}/block", cardId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(blockCardRequest)))
                    .andExpect(status().isNotFound());

            verify(cardService, times(1)).blockCard(eq(cardId), anyString());

        }

        @Test
        @DisplayName("Должен вернуть 400 если id карты неправильный")
        void returnBadRequestWhenCardIdInvalid() throws Exception {
            BlockCardRequest blockCardRequest = new BlockCardRequest("Срок истек");

            mockMvc.perform(put("/api/admin/cards/{cardId}/block", "invalid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(blockCardRequest)))
                    .andExpect(status().isBadRequest());

            verify(cardService, never()).blockCard(any(), any());

        }
    }

    @Nested
    @DisplayName("PUT /api/admin/cards/{cardId}/activate - Активация карты")
    class ActivateCardTests {

        @Test
        @DisplayName("Должен активировать карту и вернуть 200")
        void shouldActivateCardSuccessfully() throws Exception {
            Long cardId = 1L;

            when(cardService.activateCard(eq(cardId)))
                    .thenReturn(cardResponse);

            mockMvc.perform(put("/api/admin/cards/{cardId}/activate", cardId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cardId").value(cardId))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(cardService, times(1)).activateCard(eq(cardId));
        }

        @Test
        @DisplayName("Должен вернуть 409 при попытке активации активной карты")
        void shouldReturnConflictWhenActivatingActiveCard() throws Exception {
            Long cardId = 1L;

            when(cardService.activateCard(eq(cardId)))
                    .thenThrow(new CardAlreadyActiveException("Card is already active"));

            mockMvc.perform(put("/api/admin/cards/{cardId}/activate", cardId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());

            verify(cardService, times(1)).activateCard(eq(cardId));
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/cards/{cardId} - Delete Card")
    class DeleteCardTests {

        @Test
        @DisplayName("Должен удалить карту и вернуть 204")
        void DeleteCard() throws Exception {
            Long cardId = 1L;
            doNothing().when(cardService).deleteCard(eq(cardId));

            mockMvc.perform(delete("/api/admin/cards/{cardId}", cardId))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(cardService, times(1)).deleteCard(eq(cardId));
        }

        @Test
        @DisplayName("Должен вернуть 404 при попытке удаления несуществующей карты")
        void deletingNonExistentCard() throws Exception {
            Long cardId = 999L;

            doThrow(new CardNotFoundException("Карта не найдена id: " + cardId))
                    .when(cardService).deleteCard(eq(cardId));

            mockMvc.perform(delete("/api/admin/cards/{cardId}", cardId))
                    .andExpect(status().isNotFound());

            verify(cardService, times(1)).deleteCard(eq(cardId));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/cards/all - Вернуть все карты")
    class GetAllCardsTests {

        @Test
        @DisplayName("Должен вернуть список карт с пагинацией")
        void returnPaginatedCards() throws Exception {

            Pageable pageable = PageRequest.of(0, 10);
            List<CardResponse> cards = List.of(
                    cardResponse,
                    new CardResponse(                "12115678912345678912",
                            "Petrov Ivan",
                            LocalDate.now(),
                            CardStatus.ACTIVE,
                            BigDecimal.valueOf(10),
                            LocalDateTime.now(),
                            "Срок истек",
                            2L,
                            3L)
            );
            Page<CardResponse> page = new PageImpl<>(cards, pageable, cards.size());

            when(cardService.getAllCards(eq(null), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/admin/cards/all")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].cardId").value(1))
                    .andExpect(jsonPath("$.content[1].cardId").value(2))
                    .andExpect(jsonPath("$.page.size").value(10))
                    .andExpect(jsonPath("$.page.number").value(0))
                    .andExpect(jsonPath("$.page.totalElements").value(2));

            verify(cardService, times(1)).getAllCards(eq(null), any(Pageable.class));
        }

        @Test
        @DisplayName("Должен вернуть карточки по поисковому запросу")
        void returnFilteredCardsBySearch() throws Exception {

            String searchQuery = "Сабир";
            Pageable pageable = PageRequest.of(0, 10);
            List<CardResponse> filteredCards = List.of(cardResponse);
            Page<CardResponse> page = new PageImpl<>(filteredCards, pageable, filteredCards.size());

            when(cardService.getAllCards(eq(searchQuery), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/admin/cards/all")
                            .param("search", searchQuery)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].cardHolderName").value("Рашидов Сабир"))
                    .andExpect(jsonPath("$.page.totalElements").value(1));

            verify(cardService, times(1)).getAllCards(eq(searchQuery), any(Pageable.class));
        }

        @Test
        @DisplayName("Должен обработать результат пустой страницы")
        void handleEmptyPageResult() throws Exception {
            Pageable pageable = PageRequest.of(10, 10);
            Page<CardResponse> emptyPage = Page.empty(pageable);

            when(cardService.getAllCards(eq(null), any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/admin/cards/all")
                            .param("page", "10")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.page.totalElements").value(0));

            verify(cardService, times(1)).getAllCards(eq(null), any(Pageable.class));
        }
    }
}