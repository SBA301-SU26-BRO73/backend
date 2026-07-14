package com.sba301.backend.service.impl;

import com.sba301.backend.entity.User;
import com.sba301.backend.service.JwtService;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    private static final String CLAIM_USER_ID = "u_id";
    private static final String CLAIM_USER_ROLE = "u_role";
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    public String generateAccessToken(User user) {
        return buildToken(user, ACCESS_TOKEN, accessTokenExpirationMs, Map.of(
                CLAIM_USER_ID, user.getId(),
                CLAIM_USER_ROLE, user.getRole().name()
        ));
    }

    @Override
    public String generateRefreshToken(User user) {
        return buildToken(user, REFRESH_TOKEN, refreshTokenExpirationMs, Map.of());
    }

    @Override
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public Long extractUserId(String token) {
        // u_id is serialized as a JSON number; read as Number to tolerate Integer/Long.
        Number userId = parseClaims(token).get(CLAIM_USER_ID, Number.class);
        return userId == null ? null : userId.longValue();
    }

    @Override
    public String extractTokenType(String token) {
        return parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
    }

    @Override
    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_USER_ROLE, String.class);
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private String buildToken(User user, String tokenType, long expirationMs, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(extraClaims)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
