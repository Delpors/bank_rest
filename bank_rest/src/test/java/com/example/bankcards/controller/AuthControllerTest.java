package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Test")
public class AuthControllerTest {

    @Mock
    AuthenticationManager  authenticationManager;
    @Mock
    UserRepository userRepository;
    @Mock
    JwtService jwtService;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthController authController;

    private User testUser;
    private AuthRequest authRequest;
    private RegisterRequest registerRequest;
    private final String TEST_TOKEN = "test.jwt.token";
    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_FULL_NAME = "Test User";
    private final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = new User(
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FULL_NAME,
                TEST_EMAIL,
                UserRole.USER,
                null
        );

        authRequest = new AuthRequest(TEST_USERNAME, TEST_PASSWORD);

        registerRequest = new RegisterRequest(
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FULL_NAME,
                TEST_EMAIL
        );
    }

    @Nested
    @DisplayName("Тестирование авторизации")
    class loginTest {

        @Test
        void login_WithValidCredentials(){

            when(userRepository.findByUserName(TEST_USERNAME))
                    .thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any(UserPrincipal.class)))
                    .thenReturn(TEST_TOKEN);

            ResponseEntity<AuthResponse> response = authController.login(authRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().token()).isEqualTo(TEST_TOKEN);
            assertThat(response.getBody().userName()).isEqualTo(TEST_USERNAME);
            assertThat(response.getBody().fullName()).isEqualTo(TEST_FULL_NAME);
            assertThat(response.getBody().role()).isEqualTo(UserRole.USER.toString());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByUserName(TEST_USERNAME);
            verify(jwtService).generateToken(any(UserPrincipal.class));
        }

        @Test
        @DisplayName("Должен вернуть UsernameNotFoundException если пользователь не найден")
        void login_WithNonExistentUser(){

            when(userRepository.findByUserName(TEST_USERNAME)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> authController.login(authRequest));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByUserName(TEST_USERNAME);
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Должен обрабатывать сбой аутентификации")
        void login_WithInvalidCredentials(){

            doThrow(new RuntimeException())
                    .when(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            assertThrows(RuntimeException.class, () -> authController.login(authRequest));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository, never()).findByUserName(anyString());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Должен обрабатывать пустое имя пользователя")
        void login_WithNullUsername(){

            AuthRequest authRequest = new AuthRequest(null, TEST_PASSWORD);
            assertThrows(Exception.class, () -> authController.login(authRequest));
        }
    }

    @Nested
    @DisplayName("Register Endpoint Tests")
    class RegisterTests {

        @Test
        @DisplayName("Должен успешно зарегистрировать первого пользователя как ADMIN")
        void register_FirstUser(){

            when(userRepository.count()).thenReturn(0L);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn(TEST_TOKEN);

            ResponseEntity<AuthResponse> response =  authController.register(registerRequest);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().token()).isEqualTo(TEST_TOKEN);
            assertThat(response.getBody().role()).isEqualTo(UserRole.ADMIN.toString());

            verify(userRepository).count();
            verify(userRepository).save(any(User.class));
            verify(jwtService).generateToken(any(UserPrincipal.class));

        }

        @Test
        @DisplayName("Should successfully register subsequent user as USER")
        void register_SubsequentUser(){

            when(userRepository.count()).thenReturn(1L);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn(TEST_TOKEN);

            ResponseEntity<AuthResponse> register = authController.register(registerRequest);
            assertThat(register).isNotNull();
            assertThat(register.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(register.getBody()).isNotNull();
            assertThat(register.getBody().role()).isEqualTo(UserRole.USER.toString());

            verify(userRepository).count();
            verify(userRepository).save(any(User.class));
            verify(jwtService).generateToken(any(UserPrincipal.class));

        }
    }
}
