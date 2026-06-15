package com.sba301.backend.config.security;

import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.entity.User;
import com.sba301.backend.service.impl.JwtServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret",
                "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RpbmctbXVzdC1iZS1sb25nLWVub3VnaA==");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", 604800000L);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void generateAccessToken_ShouldHaveAccessTokenTypeClaim() {
        String token = jwtService.generateAccessToken(testUser);

        assertEquals("access_token", jwtService.extractTokenType(token));
    }

    @Test
    void generateRefreshToken_ShouldHaveRefreshTokenTypeClaim() {
        String token = jwtService.generateRefreshToken(testUser);

        assertEquals("refresh_token", jwtService.extractTokenType(token));
    }

    @Test
    void extractEmail_ShouldReturnCorrectSubject() {
        String token = jwtService.generateAccessToken(testUser);

        assertEquals("test@example.com", jwtService.extractEmail(token));
    }

    @Test
    void isTokenValid_ValidToken_ShouldReturnTrue() {
        String token = jwtService.generateAccessToken(testUser);

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_TamperedToken_ShouldReturnFalse() {
        String token = jwtService.generateAccessToken(testUser) + "tampered";

        assertFalse(jwtService.isTokenValid(token));
    }
}
