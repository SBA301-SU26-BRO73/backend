package com.sba301.backend.service;

import com.sba301.backend.dto.request.BranchFilterDTO;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.specification.BranchSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    // Hàm lấy tất cả (Tái sử dụng luôn Specification bằng cách truyền DTO rỗng)
    public List<Branch> getAllActiveBranches() {
        BranchFilterDTO emptyFilter = new BranchFilterDTO();
        return searchAndFilterBranches(emptyFilter);
    }

    // Hàm Search + Filter All-in-one
    public List<Branch> searchAndFilterBranches(BranchFilterDTO filterDto) {
        Specification<Branch> spec = BranchSpecification.filterByCriteria(filterDto);
        return branchRepository.findAll(spec);
    }
}