package com.sba301.backend.controller;

import com.sba301.backend.dto.request.CourtOwnerRegisterRequest;
import com.sba301.backend.dto.request.CustomerRegisterRequest;
import com.sba301.backend.dto.request.LoginRequest;
import com.sba301.backend.dto.request.RefreshTokenRequest;
import com.sba301.backend.dto.response.ApiResponse;
import com.sba301.backend.dto.response.TokenResponse;
import com.sba301.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<TokenResponse>> registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        TokenResponse response = authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Registered successfully."));
    }

    @PostMapping(value = "/register/court-owner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> registerCourtOwner(@Valid @ModelAttribute CourtOwnerRegisterRequest request) {
        authService.registerCourtOwner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Registration submitted. Awaiting admin approval."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully."));
    }
}
