package com.sba301.backend.service.impl;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.mapper.UserMapper;
import com.sba301.backend.dto.request.LoginRequest;
import com.sba301.backend.dto.request.RefreshTokenRequest;
import com.sba301.backend.dto.request.RegisterRequest;
import com.sba301.backend.dto.response.TokenResponse;
import com.sba301.backend.entity.User;
import com.sba301.backend.repository.UserRepository;
import com.sba301.backend.service.AuthService;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Override
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorEnum.EMAIL_ALREADY_EXISTS);
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return buildTokenResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
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

    private TokenResponse buildTokenResponse(User user) {
        return TokenResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .expiresIn(System.currentTimeMillis() + accessTokenExpirationMs)
                .build();
    }
}
