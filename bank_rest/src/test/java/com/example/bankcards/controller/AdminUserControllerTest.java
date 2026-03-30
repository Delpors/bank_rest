package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.TestSecurityConfig;

import com.example.bankcards.service.UserService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(value = AdminUserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@Import(TestSecurityConfig.class)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponse userResponse;
    private UserRequest userRequest;
    private Pageable pageable;

    @BeforeEach
    void setup(){
        userResponse = new UserResponse(
                1L,
                true,
                "Sabir",
                "Рашидов Сабир",
                "mi@mail.ru",
                UserRole.ADMIN,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        userRequest = new UserRequest(
                "Sabir",
                "password",
                "Рашидов Сабир",
                "mi@mail.ru",
                UserRole.ADMIN,
                1L
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Должен вернуть список все пользователей и статус 200")
    void shouldReturnAllUsers() throws Exception {
        Page<UserResponse> page = new PageImpl<>(List.of(userResponse), PageRequest.of(0, 10), 1);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("mi@mail.ru"));

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    @DisplayName("Должен создать пользователя и вернуть статус 201")
    void shouldReturnCreatedUser() throws Exception {
        when(userService.createUser(userRequest)).thenReturn(userResponse);

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userResponse.id()))
                .andExpect(jsonPath("$.active").value(userResponse.active()))
                .andExpect(jsonPath("$.userName").value(userResponse.userName()))
                .andExpect(jsonPath("$.fullName").value(userResponse.fullName()))
                .andExpect(jsonPath("$.email").value(userResponse.email()))
                .andExpect(jsonPath("$.role").value(userResponse.role().name()));


        verify(userService, times(1)).createUser(userRequest);
    }

    @Test
    @DisplayName("Должен заблокировать пользователя и вернуть статус 200")
    void shouldBlockUser() throws Exception {
        Long id = 1L;
        when(userService.blockUser(id)).thenReturn(userResponse);

        mockMvc.perform(put("/api/admin/users/{userId}/block", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).blockUser(id);
    }

    @Test
    @DisplayName("Должен активировать пользователя и вернуть статус 200")
    void shouldUnblockUser() throws Exception {
        Long id = 1L;

        when(userService.activateUser(id)).thenReturn(userResponse);

        mockMvc.perform(put("/api/admin/users/{userId}/activate", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService, times(1)).activateUser(id);
    }

    @Nested
    @DisplayName("DELETE /api/admin/users/{userId} Удалить пользователя")
    class DeleteUserTests {

        @Test
        @DisplayName("Должен удалить пользователя и вернуть статус 201")
        void shouldDeleteUser() throws Exception {
            Long id = 1L;
            doNothing().when(userService).deleteUser(id);

            mockMvc.perform(delete("/api/admin/users/{userId}", id))
                    .andDo(print())
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(userService, times(1)).deleteUser(id);
        }

        @Test
        @DisplayName("Попытка удалить несуществующего пользователя и вернуть статус 404")
        void shouldNotDeleteUser() throws Exception {
            Long id = 1L;

            doThrow(new UserNotFoundException("Пользователь с таким id не найден: " + id))
                    .when(userService).deleteUser(eq(id));
            mockMvc.perform(delete("/api/admin/users/{userId}", id))
                    .andExpect(status().isNotFound());

            verify(userService, times(1)).deleteUser(eq(id));
        }
    }
}
