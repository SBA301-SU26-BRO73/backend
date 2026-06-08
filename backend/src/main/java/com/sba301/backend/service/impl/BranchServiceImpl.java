package com.sba301.backend.service.impl;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.BranchFilterRequest;
import com.sba301.backend.dto.response.BranchResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.mapper.BranchMapper;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.specification.BranchSpecification;
import com.sba301.backend.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorEnum.RESOURCE_NOT_FOUND, "Không tìm thấy cơ sở nào với ID = " + id));
        return branchMapper.toResponse(branch);
    }

    @Override
    public Page<BranchResponse> getAllActiveBranchesPaginated(Pageable pageable) {
        Specification<Branch> spec = BranchSpecification.filterByCriteria(new BranchFilterRequest());
        return branchRepository.findAll(spec, pageable).map(branchMapper::toResponse);
    }

    @Override
    public Page<BranchResponse> searchBranchesWithPagination(BranchFilterRequest filterDto, Pageable pageable) {
        Specification<Branch> spec = BranchSpecification.filterByCriteria(filterDto);
        return branchRepository.findAll(spec, pageable).map(branchMapper::toResponse);
    }

}