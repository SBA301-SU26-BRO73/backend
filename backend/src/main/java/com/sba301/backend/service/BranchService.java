package com.sba301.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.dto.request.CreateBranchRequest;
import com.sba301.backend.dto.request.UpdateBranchRequest;
import com.sba301.backend.dto.response.BranchResponse;

/**
 * Provides CRUD operations for branches.
 */
public interface BranchService {

    /**
     * Creates a branch for an existing admin.
     *
     * @param request branch creation data
     * @return the created branch
     */
    BranchResponse create(CreateBranchRequest request);

    /**
     * Gets an active, non-deleted branch by id.
     *
     * @param id branch id
     * @return the matching branch
     */
    BranchResponse getById(Long id);

    /**
     * Gets a page of active, non-deleted branches.
     *
     * @param pageable pagination and sorting information
     * @return a page of branches
     */
    Page<BranchResponse> getAll(Pageable pageable);

    /**
     * Updates all mutable fields of a non-deleted branch.
     *
     * @param id branch id
     * @param request updated branch data
     * @return the updated branch
     */
    BranchResponse update(Long id, UpdateBranchRequest request);

    /**
     * Soft deletes a branch by changing its status to INACTIVE and setting deletedAt.
     *
     * @param id branch id
     */
    void delete(Long id);
}
