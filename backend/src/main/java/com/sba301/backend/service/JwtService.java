package com.sba301.backend.service;

import com.sba301.backend.entity.User;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    String extractEmail(String token);
    String extractTokenType(String token);
    boolean isTokenValid(String token);
}
