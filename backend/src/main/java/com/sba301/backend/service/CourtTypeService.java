package com.sba301.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.dto.request.CourtTypeRequest;
import com.sba301.backend.dto.response.CourtTypeResponse;

public interface CourtTypeService {

    Page<CourtTypeResponse> getAll(Boolean active, Pageable pageable);

    CourtTypeResponse getById(Long id);

    CourtTypeResponse create(CourtTypeRequest request);

    CourtTypeResponse update(Long id, CourtTypeRequest request);

    void delete(Long id);
}
