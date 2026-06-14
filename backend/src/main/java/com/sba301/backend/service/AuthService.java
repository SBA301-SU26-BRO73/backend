package com.sba301.backend.service;

import com.sba301.backend.dto.request.CourtOwnerRegisterRequest;
import com.sba301.backend.dto.request.CustomerRegisterRequest;
import com.sba301.backend.dto.request.LoginRequest;
import com.sba301.backend.dto.request.RefreshTokenRequest;
import com.sba301.backend.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse registerCustomer(CustomerRegisterRequest request);
    void registerCourtOwner(CourtOwnerRegisterRequest request);
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(RefreshTokenRequest request);
}
