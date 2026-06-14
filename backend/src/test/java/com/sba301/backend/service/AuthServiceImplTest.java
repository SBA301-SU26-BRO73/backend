package com.sba301.backend.service;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.mapper.UserMapper;
import com.sba301.backend.dto.request.CourtOwnerRegisterRequest;
import com.sba301.backend.dto.request.CustomerRegisterRequest;
import com.sba301.backend.dto.request.LoginRequest;
import com.sba301.backend.dto.request.RefreshTokenRequest;
import com.sba301.backend.dto.response.TokenResponse;
import com.sba301.backend.entity.User;
import com.sba301.backend.repository.UserDocumentRepository;
import com.sba301.backend.repository.UserRepository;
import com.sba301.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserDocumentRepository userDocumentRepository;
    @Mock private UserMapper userMapper;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeCustomer;
    private User pendingAdmin;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpirationMs", 3600000L);

        activeCustomer = User.builder()
                .id(1L)
                .email("customer@example.com")
                .passwordHash("hashedPassword")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        pendingAdmin = User.builder()
                .id(2L)
                .email("owner@example.com")
                .passwordHash("hashedPassword")
                .role(UserRole.ADMIN)
                .status(UserStatus.PENDING_APPROVAL)
                .build();
    }

    @Test
    void registerCustomer_Success_ShouldReturnTokens() {
        CustomerRegisterRequest request = new CustomerRegisterRequest("Nguyen Van A", "customer@example.com", "0901234567", "password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toCustomerEntity(request)).thenReturn(activeCustomer);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(activeCustomer);
        when(jwtService.generateAccessToken(activeCustomer)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(activeCustomer)).thenReturn("refresh-token");

        TokenResponse result = authService.registerCustomer(request);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerCustomer_EmailAlreadyExists_ShouldThrow() {
        CustomerRegisterRequest request = new CustomerRegisterRequest("Nguyen Van A", "exists@example.com", "0901234567", "password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.registerCustomer(request));

        assertEquals(ErrorEnum.EMAIL_ALREADY_EXISTS, ex.getErrorEnum());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerCourtOwner_Success_ShouldSaveUserAndDocuments() {
        MockMultipartFile legalDoc = new MockMultipartFile("legalDocuments", "cmnd.jpg", "image/jpeg", "data".getBytes());
        CourtOwnerRegisterRequest request = new CourtOwnerRegisterRequest(
                "owner@example.com", "0901234567", "password12345", List.of(legalDoc), null
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toCourtOwnerEntity(request)).thenReturn(pendingAdmin);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(pendingAdmin);
        when(fileStorageService.storeFiles(any(), eq("legal/2"))).thenReturn(List.of("/uploads/legal/2/cmnd.jpg"));

        assertDoesNotThrow(() -> authService.registerCourtOwner(request));

        verify(userRepository).save(any(User.class));
        verify(userDocumentRepository, atLeastOnce()).save(any());
    }

    @Test
    void registerCourtOwner_EmailAlreadyExists_ShouldThrow() {
        MockMultipartFile legalDoc = new MockMultipartFile("legalDocuments", "cmnd.jpg", "image/jpeg", "data".getBytes());
        CourtOwnerRegisterRequest request = new CourtOwnerRegisterRequest(
                "exists@example.com", "0901234567", "password12345", List.of(legalDoc), null
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.registerCourtOwner(request));

        assertEquals(ErrorEnum.EMAIL_ALREADY_EXISTS, ex.getErrorEnum());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success_ShouldReturnTokens() {
        LoginRequest request = new LoginRequest("customer@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findActiveByEmail(request.getEmail())).thenReturn(Optional.of(activeCustomer));
        when(jwtService.generateAccessToken(activeCustomer)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(activeCustomer)).thenReturn("refresh-token");

        TokenResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    void login_WrongPassword_ShouldThrowInvalidCredentials() {
        LoginRequest request = new LoginRequest("customer@example.com", "wrongpassword");

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
        when(jwtService.extractEmail(request.getRefreshToken())).thenReturn("customer@example.com");
        when(userRepository.findActiveByEmail("customer@example.com")).thenReturn(Optional.of(activeCustomer));
        when(jwtService.generateAccessToken(activeCustomer)).thenReturn("new-access-token");

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
