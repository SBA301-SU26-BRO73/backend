package com.sba301.backend.service.impl;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.CreateStaffRequest;
import com.sba301.backend.dto.request.UpdateStaffRequest;
import com.sba301.backend.dto.response.StaffResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.Staff;
import com.sba301.backend.entity.User;
import com.sba301.backend.mapper.StaffMapper;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.StaffRepository;
import com.sba301.backend.repository.UserRepository;
import com.sba301.backend.service.StaffService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final StaffMapper staffMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StaffResponse create(CreateStaffRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new AppException(ErrorEnum.USER_EMAIL_ALREADY_EXISTS);
        }

        Branch branch = getBranch(request.getBranchId());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(UserRole.STAFF);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(branch);

        return staffMapper.toResponse(staffRepository.save(staff));
    }

    @Override
    public StaffResponse getById(Long id) {
        return staffMapper.toResponse(getStaff(id));
    }

    @Override
    public Page<StaffResponse> getByBranch(Long branchId, Pageable pageable) {
        getBranch(branchId);
        return staffRepository.findAllByBranchIdAndDeletedAtIsNull(branchId, pageable)
                .map(staffMapper::toResponse);
    }

    @Override
    @Transactional
    public StaffResponse update(Long id, UpdateStaffRequest request) {
        Staff staff = getStaff(id);

        if (request.getBranchId() != null) {
            staff.setBranch(getBranch(request.getBranchId()));
        }

        if (request.getPhone() != null) {
            staff.getUser().setPhone(request.getPhone());
        }

        return staffMapper.toResponse(staffRepository.save(staff));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Staff staff = getStaff(id);
        staff.setDeletedAt(OffsetDateTime.now());
        staff.getUser().setStatus(UserStatus.INACTIVE);
        userRepository.save(staff.getUser());
        staffRepository.save(staff);
    }

    private Staff getStaff(Long id) {
        return staffRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorEnum.STAFF_NOT_FOUND));
    }

    private Branch getBranch(Long branchId) {
        return branchRepository.findByIdAndStatusAndDeletedAtIsNull(branchId, BranchStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorEnum.BRANCH_NOT_FOUND));
    }
}
