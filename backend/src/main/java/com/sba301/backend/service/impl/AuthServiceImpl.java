package com.sba301.backend.service.impl;

import com.sba301.backend.common.enums.DocumentType;
import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.mapper.UserMapper;
import com.sba301.backend.dto.request.CourtOwnerRegisterRequest;
import com.sba301.backend.dto.request.CustomerRegisterRequest;
import com.sba301.backend.dto.request.LoginRequest;
import com.sba301.backend.dto.request.RefreshTokenRequest;
import com.sba301.backend.dto.response.TokenResponse;
import com.sba301.backend.entity.User;
import com.sba301.backend.entity.UserDocument;
import com.sba301.backend.repository.UserDocumentRepository;
import com.sba301.backend.repository.UserRepository;
import com.sba301.backend.service.AuthService;
import com.sba301.backend.service.FileStorageService;
import com.sba301.backend.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final FileStorageService fileStorageService;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Override
    @Transactional
    public TokenResponse registerCustomer(CustomerRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorEnum.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorEnum.PHONE_ALREADY_EXISTS);
        }

        User user = userMapper.toCustomerEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return buildTokenResponse(user);
    }

    @Override
    @Transactional
    public void registerCourtOwner(CourtOwnerRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorEnum.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorEnum.PHONE_ALREADY_EXISTS);
        }

        User user = userMapper.toCourtOwnerEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        saveDocuments(user, request);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorEnum.INVALID_CREDENTIALS);
        } catch (DisabledException e) {
            throw new AppException(ErrorEnum.ACCOUNT_INACTIVE);
        }

        User user = userRepository.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorEnum.USER_NOT_FOUND));

        return buildTokenResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtService.isTokenValid(token)) {
            throw new AppException(ErrorEnum.INVALID_TOKEN);
        }

        if (!"refresh_token".equals(jwtService.extractTokenType(token))) {
            throw new AppException(ErrorEnum.INVALID_TOKEN);
        }

        User user = userRepository.findActiveByEmail(jwtService.extractEmail(token))
                .orElseThrow(() -> new AppException(ErrorEnum.USER_NOT_FOUND));

        return TokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(token)
                .expiresIn(System.currentTimeMillis() + accessTokenExpirationMs)
                .build();
    }

    private void saveDocuments(User user, CourtOwnerRegisterRequest request) {
        List<String> legalUrls = fileStorageService.storeFiles(request.getLegalDocuments(), "legal/" + user.getId());
        legalUrls.stream()
                .map(url -> UserDocument.builder().user(user).url(url).docType(DocumentType.LEGAL).build())
                .forEach(userDocumentRepository::save);

        if (request.getCourtImages() != null && !request.getCourtImages().isEmpty()) {
            List<String> imageUrls = fileStorageService.storeFiles(request.getCourtImages(), "court/" + user.getId());
            imageUrls.stream()
                    .map(url -> UserDocument.builder().user(user).url(url).docType(DocumentType.COURT_IMAGE).build())
                    .forEach(userDocumentRepository::save);
        }
    }

    private TokenResponse buildTokenResponse(User user) {
        return TokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .expiresIn(System.currentTimeMillis() + accessTokenExpirationMs)
                .build();
    }
}
