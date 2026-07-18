package com.sba301.backend.service.impl;

import java.time.OffsetDateTime;

import com.sba301.backend.dto.request.BranchFilterRequest;
import com.sba301.backend.repository.specifacation.BranchSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.common.enums.UserRole;
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
import com.sba301.backend.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final BranchMapper branchMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public BranchResponse create(CreateBranchRequest request) {
        validateUniqueName(request.getName());
        User admin = resolveRequestedAdmin(request.getAdminId());

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
        User currentUser = currentUserService.getCurrentUserOptional().orElse(null);
        Page<Branch> branches;
        if (currentUser == null) {
            branches = branchRepository.findAllByStatusAndDeletedAtIsNull(BranchStatus.ACTIVE, pageable);
        } else if (currentUserService.isSuperAdmin(currentUser)) {
            branches = branchRepository.findAllByStatusAndDeletedAtIsNull(BranchStatus.ACTIVE, pageable);
        } else if (currentUser.getRole() == UserRole.ADMIN) {
            branches = branchRepository.findAllByAdminIdAndStatusAndDeletedAtIsNull(
                    currentUser.getId(), BranchStatus.ACTIVE, pageable);
        } else {
            branches = branchRepository.findAllByStatusAndDeletedAtIsNull(BranchStatus.ACTIVE, pageable);
        }

        return branches
                .map(branchMapper::toResponse);
    }

    @Override
    @Transactional
    public BranchResponse update(Long id, UpdateBranchRequest request) {
        Branch branch = getAccessibleBranch(id);

        if (!branch.getName().equals(request.getName())) {
            validateUniqueName(request.getName());
        }

        User admin = resolveRequestedAdmin(request.getAdminId());
        branch.setAdmin(admin);
        branchMapper.updateEntity(branch, request);

        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Branch branch = getAccessibleBranch(id);
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

    private Branch getAccessibleBranch(Long id) {
        Branch branch = getBranch(id);
        User currentUser = currentUserService.getCurrentUser();
        if (!currentUserService.isSuperAdmin(currentUser)
                && !branch.getAdmin().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorEnum.ACCESS_DENIED);
        }
        return branch;
    }

    private User resolveRequestedAdmin(Long requestedAdminId) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUserService.isSuperAdmin(currentUser)) {
            return getAdmin(requestedAdminId);
        }
        if (!currentUser.getId().equals(requestedAdminId)) {
            throw new AppException(ErrorEnum.ACCESS_DENIED);
        }
        return currentUser;
    }

    private void validateUniqueName(String name) {
        if (branchRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new AppException(ErrorEnum.BRANCH_NAME_ALREADY_EXISTS);
        }
    }

    @Override
    public Page<BranchResponse> searchBranchesWithPagination(BranchFilterRequest filterDto, Pageable pageable) {
        User currentUser = currentUserService.getCurrentUserOptional().orElse(null);
        Long adminId = null;
        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            adminId = currentUser.getId();
        }
        Specification<Branch> spec = BranchSpecification.filterByCriteria(filterDto, adminId);
        Page<Branch> branchPage = branchRepository.findAll(spec, pageable);
        return branchPage.map(branchMapper::toResponse);
    }
}
