package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.TestSecurityConfig;
import com.example.bankcards.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ))
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private final String validUsername = "john_doe";
    private final String validPassword = "password123";
    private final String validFullName = "John Doe";
    private final String validEmail = "john@example.com";
    private final String mockToken = "mock.jwt.token";
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserName(validUsername);
        sampleUser.setPassword("encodedPassword");
        sampleUser.setFullName(validFullName);
        sampleUser.setEmail(validEmail);
        sampleUser.setRol(UserRole.USER);
    }

    @Test
    void login_success_returnsAuthResponse() throws Exception {

        AuthRequest request = new AuthRequest(validUsername, validPassword);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUserName(validUsername)).thenReturn(Optional.of(sampleUser));
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn(mockToken);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken))
                .andExpect(jsonPath("$.userName").value(validUsername))
                .andExpect(jsonPath("$.fullName").value(validFullName))
                .andExpect(jsonPath("$.role").value(UserRole.USER.toString()));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUserName(validUsername);
        verify(jwtService).generateToken(any(UserPrincipal.class));
    }

    @Test
    void login_badCredentials_returnsUnauthorized() throws Exception {

        AuthRequest request = new AuthRequest(validUsername, "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUserName(anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_userNotFound_returnsNotFound() throws Exception {

        AuthRequest request = new AuthRequest(validUsername, validPassword);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByUserName(validUsername)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUserName(validUsername);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_firstUser_setsAdminRoleAndReturnsAuthResponse() throws Exception {

        RegisterRequest request = new RegisterRequest(validUsername, validPassword, validFullName, validEmail);

        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(validPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserName(validUsername);
            saved.setFullName(validFullName);
            saved.setEmail(validEmail);
            saved.setRol(UserRole.ADMIN);
            return saved;
        });
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn(mockToken);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken))
                .andExpect(jsonPath("$.userName").value(validUsername))
                .andExpect(jsonPath("$.fullName").value(validFullName))
                .andExpect(jsonPath("$.role").value(UserRole.ADMIN.toString()));

        verify(userRepository).count();
        verify(passwordEncoder).encode(validPassword);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(UserPrincipal.class));
    }

    @Test
    void register_nonFirstUser_setsUserRoleAndReturnsAuthResponse() throws Exception {

        RegisterRequest request = new RegisterRequest(validUsername, validPassword, validFullName, validEmail);

        when(userRepository.count()).thenReturn(1L);
        when(passwordEncoder.encode(validPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserName(validUsername);
            saved.setFullName(validFullName);
            saved.setEmail(validEmail);
            saved.setRol(UserRole.USER);
            return saved;
        });
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn(mockToken);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken))
                .andExpect(jsonPath("$.userName").value(validUsername))
                .andExpect(jsonPath("$.fullName").value(validFullName))
                .andExpect(jsonPath("$.role").value(UserRole.USER.toString()));

        verify(userRepository).count();
        verify(passwordEncoder).encode(validPassword);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(UserPrincipal.class));
    }

    @Test
    void register_duplicateUser_returnsInternalServerError() throws Exception {

        RegisterRequest request = new RegisterRequest(validUsername, validPassword, validFullName, validEmail);

        when(userRepository.count()).thenReturn(1L);
        when(passwordEncoder.encode(validPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate user"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(userRepository).count();
        verify(passwordEncoder).encode(validPassword);
        verify(userRepository).save(any(User.class));
        verify(jwtService, never()).generateToken(any());
    }
}