package com.sba301.backend.service;

import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getAll(UserRole role, UserStatus status, Pageable pageable);
    UserResponse approve(Long id);
    UserResponse reject(Long id);
    UserResponse lock(Long id);
    UserResponse unlock(Long id);
}
