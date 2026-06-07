package com.sba301.backend.service.impl;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException; // IMPORT MỚI
import com.sba301.backend.dto.request.BranchFilterDTO;
import com.sba301.backend.dto.response.BranchResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.specification.BranchSpecification;
import com.sba301.backend.service.BranchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    public BranchServiceImpl(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .ward(branch.getWard())
                .city(branch.getCity())
                .phone(branch.getPhone())
                .status(branch.getStatus())
                .createdAt(branch.getCreatedAt() != null ? branch.getCreatedAt().toLocalDateTime() : null)
                .build();
    }

    @Override
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorEnum.RESOURCE_NOT_FOUND, "Không tìm thấy cơ sở nào với ID = " + id));
        return mapToResponse(branch);
    }

    @Override
    public List<BranchResponse> getAllActiveBranches() {
        Specification<Branch> spec = BranchSpecification.filterByCriteria(new BranchFilterDTO());
        return branchRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BranchResponse> getAllActiveBranchesPaginated(Pageable pageable) {
        Specification<Branch> spec = BranchSpecification.filterByCriteria(new BranchFilterDTO());
        return branchRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    public Page<BranchResponse> searchBranchesWithPagination(BranchFilterDTO filterDto, Pageable pageable) {
        Specification<Branch> spec = BranchSpecification.filterByCriteria(filterDto);
        return branchRepository.findAll(spec, pageable).map(this::mapToResponse);
    }
}