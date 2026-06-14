package com.sba301.backend.service.impl;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.CreateBranchRequest;
import com.sba301.backend.dto.request.UpdateBranchRequest;
import com.sba301.backend.dto.response.BranchResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.User;
import com.sba301.backend.mapper.BranchMapper;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.UserRepository;
import com.sba301.backend.service.BranchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final BranchMapper branchMapper;

    @Override
    @Transactional
    public BranchResponse create(CreateBranchRequest request) {
        validateUniqueName(request.getName());
        User admin = getAdmin(request.getAdminId());

        Branch branch = branchMapper.toEntity(request);
        branch.setAdmin(admin);

        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    public BranchResponse getById(Long id) {
        return branchMapper.toResponse(getBranch(id));
    }

    @Override
    public Page<BranchResponse> getAll(Pageable pageable) {
        return branchRepository.findAllByStatusAndDeletedAtIsNull(BranchStatus.ACTIVE, pageable)
                .map(branchMapper::toResponse);
    }

    @Override
    @Transactional
    public BranchResponse update(Long id, UpdateBranchRequest request) {
        Branch branch = getBranch(id);

        if (!branch.getName().equals(request.getName())) {
            validateUniqueName(request.getName());
        }

        User admin = getAdmin(request.getAdminId());
        branch.setAdmin(admin);
        branchMapper.updateEntity(branch, request);

        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Branch branch = getBranch(id);
        branch.setStatus(BranchStatus.INACTIVE);
        branch.setDeletedAt(OffsetDateTime.now());
        branchRepository.save(branch);
    }

    private Branch getBranch(Long id) {
        return branchRepository.findByIdAndStatusAndDeletedAtIsNull(id, BranchStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorEnum.BRANCH_NOT_FOUND));
    }

    private User getAdmin(Long adminId) {
        return userRepository.findByIdAndDeletedAtIsNull(adminId)
                .orElseThrow(() -> new AppException(ErrorEnum.ADMIN_NOT_FOUND));
    }

    private void validateUniqueName(String name) {
        if (branchRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new AppException(ErrorEnum.BRANCH_NAME_ALREADY_EXISTS);
        }
    }
}
