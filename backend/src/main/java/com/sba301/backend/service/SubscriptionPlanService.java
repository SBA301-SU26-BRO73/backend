package com.sba301.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.dto.request.SubscriptionPlanRequest;
import com.sba301.backend.dto.response.SubscriptionPlanResponse;

public interface SubscriptionPlanService {

    Page<SubscriptionPlanResponse> getAll(Pageable pageable);

    SubscriptionPlanResponse getById(Long id);

    SubscriptionPlanResponse create(SubscriptionPlanRequest request);

    SubscriptionPlanResponse update(Long id, SubscriptionPlanRequest request);

    void delete(Long id);
}
