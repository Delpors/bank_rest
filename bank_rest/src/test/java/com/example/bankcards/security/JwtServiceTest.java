package com.example.bankcards.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET_KEY =
            "c2VjcmV0LWtleS1mb3Itand0LXRlc3RpbmctbXVzdC1iZS1sb25nLWVub3VnaC1hbmQtc2VjdXJlLWhzMjU2LXNpZ25pbmc=";
    private static final long EXPIRATION_MS = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MS);
    }

    @Test
    void generateToken_shouldCreateValidToken() {

        UserPrincipal userDetails = mock(UserPrincipal.class);
        when(userDetails.getUsername()).thenReturn("testuser");

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotEmpty();

        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    void generateToken_withClaims_shouldIncludeClaims() {

        UserPrincipal userDetails = mock(UserPrincipal.class);
        when(userDetails.getUsername()).thenReturn("john");
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        claims.put("custom", "value");

        String token = jwtService.generateToken(claims, userDetails);

        String extractedRole = jwtService.extractClaim(token, c -> c.get("role", String.class));
        assertThat(extractedRole).isEqualTo("ADMIN");
        String extractedCustom = jwtService.extractClaim(token, c -> c.get("custom", String.class));
        assertThat(extractedCustom).isEqualTo("value");
        String extractedSubject = jwtService.extractUsername(token);
        assertThat(extractedSubject).isEqualTo("john");
    }

    @Test
    void extractUsername_shouldReturnSubject() {
        UserPrincipal userDetails = mock(UserPrincipal.class);
        when(userDetails.getUsername()).thenReturn("alice");
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("alice");
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {

        UserPrincipal userDetails = mock(UserPrincipal.class);
        when(userDetails.getUsername()).thenReturn("bob");
        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseWhenUsernameMismatch() {

        UserPrincipal userDetails = mock(UserPrincipal.class);
        when(userDetails.getUsername()).thenReturn("bob");
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = mock(UserDetails.class);
        when(otherUser.getUsername()).thenReturn("eve");

        boolean valid = jwtService.isTokenValid(token, otherUser);

        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalseWhenTokenExpired() throws InterruptedException {

        ReflectionTestUtils.setField(jwtService, "expiration", 100L);
        UserPrincipal userDetails = mock(UserPrincipal.class);
        when(userDetails.getUsername()).thenReturn("charlie");
        String token = jwtService.generateToken(userDetails);

        TimeUnit.MILLISECONDS.sleep(200);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isFalse();
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MS);
    }

    @Test
    void isTokenValid_shouldReturnFalseForMalformedToken() {

        String malformedToken = "eyJhbGciOiJIUzI1NiJ9.INVALID";

        UserPrincipal userDetails = mock(UserPrincipal.class);
        when(userDetails.getUsername()).thenReturn("any");

        org.junit.jupiter.api.Assertions.assertThrows(
                io.jsonwebtoken.JwtException.class,
                () -> jwtService.isTokenValid(malformedToken, userDetails)
        );
    }

    @Test
    void extractUsername_shouldThrowExceptionForMalformedToken() {
        String malformed = "invalid.token";
        org.junit.jupiter.api.Assertions.assertThrows(
                io.jsonwebtoken.JwtException.class,
                () -> jwtService.extractUsername(malformed)
        );
    }
}