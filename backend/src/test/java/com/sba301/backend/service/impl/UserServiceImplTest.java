package com.sba301.backend.service.impl;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.mapper.UserMapper;
import com.sba301.backend.dto.response.UserResponse;
import com.sba301.backend.entity.User;
import com.sba301.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User pendingAdmin;
    private User activeCustomer;
    private User lockedUser;
    private UserResponse userResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        pendingAdmin = User.builder()
                .id(1L).email("admin@example.com")
                .role(UserRole.ADMIN).status(UserStatus.PENDING_APPROVAL).build();

        activeCustomer = User.builder()
                .id(2L).email("customer@example.com")
                .role(UserRole.CUSTOMER).status(UserStatus.ACTIVE).build();

        lockedUser = User.builder()
                .id(3L).email("locked@example.com")
                .role(UserRole.CUSTOMER).status(UserStatus.LOCKED).build();

        userResponse = UserResponse.builder().id(1L).email("admin@example.com").build();
    }

    @Test
    void getAll_NoFilter_ShouldReturnAllActive() {
        Page<User> page = new PageImpl<>(List.of(pendingAdmin, activeCustomer));
        when(userRepository.findAllActive(pageable)).thenReturn(page);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        Page<UserResponse> result = userService.getAll(null, null, pageable);

        assertEquals(2, result.getTotalElements());
        verify(userRepository).findAllActive(pageable);
    }

    @Test
    void getAll_FilterByRole_ShouldCallFindAllByRole() {
        when(userRepository.findAllByRole(UserRole.ADMIN, pageable)).thenReturn(new PageImpl<>(List.of(pendingAdmin)));
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        userService.getAll(UserRole.ADMIN, null, pageable);

        verify(userRepository).findAllByRole(UserRole.ADMIN, pageable);
    }

    @Test
    void getAll_FilterByStatus_ShouldCallFindAllByStatus() {
        when(userRepository.findAllByStatus(UserStatus.PENDING_APPROVAL, pageable)).thenReturn(new PageImpl<>(List.of(pendingAdmin)));
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        userService.getAll(null, UserStatus.PENDING_APPROVAL, pageable);

        verify(userRepository).findAllByStatus(UserStatus.PENDING_APPROVAL, pageable);
    }

    @Test
    void getAll_FilterByBoth_ShouldCallFindAllByRoleAndStatus() {
        when(userRepository.findAllByRoleAndStatus(UserRole.ADMIN, UserStatus.PENDING_APPROVAL, pageable))
                .thenReturn(new PageImpl<>(List.of(pendingAdmin)));
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        userService.getAll(UserRole.ADMIN, UserStatus.PENDING_APPROVAL, pageable);

        verify(userRepository).findAllByRoleAndStatus(UserRole.ADMIN, UserStatus.PENDING_APPROVAL, pageable);
    }

    @Test
    void approve_Success_ShouldSetStatusActive() {
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(pendingAdmin));
        when(userRepository.save(any())).thenReturn(pendingAdmin);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        UserResponse result = userService.approve(1L);

        assertEquals(UserStatus.ACTIVE, pendingAdmin.getStatus());
        assertNotNull(result);
    }

    @Test
    void approve_UserNotFound_ShouldThrow() {
        when(userRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userService.approve(99L));
        assertEquals(ErrorEnum.USER_NOT_FOUND, ex.getErrorEnum());
    }

    @Test
    void approve_UserNotPending_ShouldThrow() {
        when(userRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(activeCustomer));

        AppException ex = assertThrows(AppException.class, () -> userService.approve(2L));
        assertEquals(ErrorEnum.USER_NOT_PENDING, ex.getErrorEnum());
    }

    @Test
    void reject_Success_ShouldSetStatusInactive() {
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(pendingAdmin));
        when(userRepository.save(any())).thenReturn(pendingAdmin);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        userService.reject(1L);

        assertEquals(UserStatus.INACTIVE, pendingAdmin.getStatus());
    }

    @Test
    void reject_UserNotFound_ShouldThrow() {
        when(userRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userService.reject(99L));
        assertEquals(ErrorEnum.USER_NOT_FOUND, ex.getErrorEnum());
    }

    @Test
    void reject_UserNotPending_ShouldThrow() {
        when(userRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(activeCustomer));

        AppException ex = assertThrows(AppException.class, () -> userService.reject(2L));
        assertEquals(ErrorEnum.USER_NOT_PENDING, ex.getErrorEnum());
    }

    @Test
    void lock_Success_ShouldSetStatusLocked() {
        when(userRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(activeCustomer));
        when(userRepository.save(any())).thenReturn(activeCustomer);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        userService.lock(2L);

        assertEquals(UserStatus.LOCKED, activeCustomer.getStatus());
    }

    @Test
    void lock_UserNotFound_ShouldThrow() {
        when(userRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userService.lock(99L));
        assertEquals(ErrorEnum.USER_NOT_FOUND, ex.getErrorEnum());
    }

    @Test
    void lock_AlreadyLocked_ShouldThrow() {
        when(userRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.of(lockedUser));

        AppException ex = assertThrows(AppException.class, () -> userService.lock(3L));
        assertEquals(ErrorEnum.USER_ALREADY_LOCKED, ex.getErrorEnum());
    }

    @Test
    void unlock_Success_ShouldSetStatusActive() {
        when(userRepository.findByIdAndDeletedAtIsNull(3L)).thenReturn(Optional.of(lockedUser));
        when(userRepository.save(any())).thenReturn(lockedUser);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        userService.unlock(3L);

        assertEquals(UserStatus.ACTIVE, lockedUser.getStatus());
    }

    @Test
    void unlock_UserNotFound_ShouldThrow() {
        when(userRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userService.unlock(99L));
        assertEquals(ErrorEnum.USER_NOT_FOUND, ex.getErrorEnum());
    }

    @Test
    void unlock_NotLocked_ShouldThrow() {
        when(userRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(activeCustomer));

        AppException ex = assertThrows(AppException.class, () -> userService.unlock(2L));
        assertEquals(ErrorEnum.USER_NOT_LOCKED, ex.getErrorEnum());
    }
}
