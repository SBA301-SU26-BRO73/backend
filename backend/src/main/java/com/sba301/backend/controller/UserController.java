package com.sba301.backend.controller;

import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.dto.response.ApiResponse;
import com.sba301.backend.dto.response.UserResponse;
import com.sba301.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAll(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAll(role, status, pageable)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.approve(id), "User approved successfully."));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.reject(id), "User rejected successfully."));
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> lock(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.lock(id), "User locked successfully."));
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> unlock(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.unlock(id), "User unlocked successfully."));
    }
}
