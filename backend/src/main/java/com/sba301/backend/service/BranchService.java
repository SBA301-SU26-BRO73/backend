package com.sba301.backend.service;

import com.sba301.backend.dto.request.BranchFilterDTO;
import com.sba301.backend.dto.response.BranchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BranchService {

    BranchResponse getBranchById(Long id);

    List<BranchResponse> getAllActiveBranches();

    Page<BranchResponse> getAllActiveBranchesPaginated(Pageable pageable);

    Page<BranchResponse> searchBranchesWithPagination(BranchFilterDTO filterDto, Pageable pageable);
}