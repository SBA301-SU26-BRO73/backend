package com.sba301.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.dto.request.CreateStaffRequest;
import com.sba301.backend.dto.request.UpdateStaffRequest;
import com.sba301.backend.dto.response.StaffResponse;

public interface StaffService {

    StaffResponse create(CreateStaffRequest request);

    StaffResponse getById(Long id);

    Page<StaffResponse> getByBranch(Long branchId, Pageable pageable);

    StaffResponse update(Long id, UpdateStaffRequest request);

    void delete(Long id);
}
