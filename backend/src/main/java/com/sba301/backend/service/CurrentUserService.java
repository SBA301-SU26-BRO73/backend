package com.sba301.backend.service;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.entity.User;
import com.sba301.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorEnum.UNAUTHORIZED);
        }

        String email = extractEmail(authentication.getPrincipal());
        if (email == null) {
            throw new AppException(ErrorEnum.UNAUTHORIZED);
        }

        User user = userRepository.findActiveByEmail(email)
                .orElseThrow(() -> new AppException(ErrorEnum.UNAUTHORIZED));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorEnum.ACCOUNT_INACTIVE);
        }
        return user;
    }

    public Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = extractEmail(authentication.getPrincipal());
        if (email == null) {
            return Optional.empty();
        }

        return userRepository.findActiveByEmail(email)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE);
    }

    public boolean isSuperAdmin(User user) {
        return user.getRole() == UserRole.SUPER_ADMIN;
    }

    private String extractEmail(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String email && !"anonymousUser".equals(email)) {
            return email;
        }
        return null;
    }
}
