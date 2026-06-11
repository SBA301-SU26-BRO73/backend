package com.sba301.backend.service;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.mapper.UserMapper;
import com.sba301.backend.dto.request.LoginRequest;
import com.sba301.backend.dto.request.RefreshTokenRequest;
import com.sba301.backend.dto.request.RegisterRequest;
import com.sba301.backend.dto.response.TokenResponse;
import com.sba301.backend.entity.User;
import com.sba301.backend.repository.UserRepository;
import com.sba301.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpirationMs", 3600000L);

        activeUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void register_Success_ShouldSaveUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", null);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(activeUser);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);
        when(jwtService.generateAccessToken(activeUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(activeUser)).thenReturn("refresh-token");

        TokenResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ShouldThrowEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("exists@example.com", "password123", null);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.register(request));

        assertEquals(ErrorEnum.EMAIL_ALREADY_EXISTS, ex.getErrorEnum());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success_ShouldReturnTokens() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findActiveByEmail(request.getEmail())).thenReturn(Optional.of(activeUser));
        when(jwtService.generateAccessToken(activeUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(activeUser)).thenReturn("refresh-token");

        TokenResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    void login_WrongPassword_ShouldThrowInvalidCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AppException ex = assertThrows(AppException.class, () -> authService.login(request));

        assertEquals(ErrorEnum.INVALID_CREDENTIALS, ex.getErrorEnum());
    }

    @Test
    void login_InactiveUser_ShouldThrowAccountInactive() {
        LoginRequest request = new LoginRequest("inactive@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User is disabled"));

        AppException ex = assertThrows(AppException.class, () -> authService.login(request));

        assertEquals(ErrorEnum.ACCOUNT_INACTIVE, ex.getErrorEnum());
    }

    @Test
    void refresh_Success_ShouldReturnNewAccessToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        when(jwtService.isTokenValid(request.getRefreshToken())).thenReturn(true);
        when(jwtService.extractTokenType(request.getRefreshToken())).thenReturn("refresh_token");
        when(jwtService.extractEmail(request.getRefreshToken())).thenReturn("test@example.com");
        when(userRepository.findActiveByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(jwtService.generateAccessToken(activeUser)).thenReturn("new-access-token");

        TokenResponse result = authService.refresh(request);

        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("valid-refresh-token", result.getRefreshToken());
    }

    @Test
    void refresh_InvalidToken_ShouldThrowInvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

        when(jwtService.isTokenValid(request.getRefreshToken())).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authService.refresh(request));

        assertEquals(ErrorEnum.INVALID_TOKEN, ex.getErrorEnum());
    }

    @Test
    void refresh_AccessTokenPassedAsRefresh_ShouldThrowInvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("access-token");

        when(jwtService.isTokenValid(request.getRefreshToken())).thenReturn(true);
        when(jwtService.extractTokenType(request.getRefreshToken())).thenReturn("access_token");

        AppException ex = assertThrows(AppException.class, () -> authService.refresh(request));

        assertEquals(ErrorEnum.INVALID_TOKEN, ex.getErrorEnum());
    }
}
