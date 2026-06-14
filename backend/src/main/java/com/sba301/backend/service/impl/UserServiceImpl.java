package com.sba301.backend.service.impl;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.mapper.UserMapper;
import com.sba301.backend.dto.response.UserResponse;
import com.sba301.backend.entity.User;
import com.sba301.backend.repository.UserRepository;
import com.sba301.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Page<UserResponse> getAll(UserRole role, UserStatus status, Pageable pageable) {
        Page<User> users;
        if (role != null && status != null) {
            users = userRepository.findAllByRoleAndStatus(role, status, pageable);
        } else if (role != null) {
            users = userRepository.findAllByRole(role, pageable);
        } else if (status != null) {
            users = userRepository.findAllByStatus(status, pageable);
        } else {
            users = userRepository.findAllActive(pageable);
        }
        return users.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse approve(Long id) {
        User user = getActiveUser(id);
        if (user.getRole() != UserRole.ADMIN || user.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorEnum.USER_NOT_PENDING);
        }
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse reject(Long id) {
        User user = getActiveUser(id);
        if (user.getRole() != UserRole.ADMIN || user.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new AppException(ErrorEnum.USER_NOT_PENDING);
        }
        user.setStatus(UserStatus.INACTIVE);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse lock(Long id) {
        User user = getActiveUser(id);
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AppException(ErrorEnum.USER_ALREADY_LOCKED);
        }
        user.setStatus(UserStatus.LOCKED);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse unlock(Long id) {
        User user = getActiveUser(id);
        if (user.getStatus() != UserStatus.LOCKED) {
            throw new AppException(ErrorEnum.USER_NOT_LOCKED);
        }
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toResponse(userRepository.save(user));
    }

    private User getActiveUser(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorEnum.USER_NOT_FOUND));
    }
}
