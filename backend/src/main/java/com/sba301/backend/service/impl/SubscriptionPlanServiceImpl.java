package com.sba301.backend.service.impl;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.SubscriptionPlanRequest;
import com.sba301.backend.dto.response.SubscriptionPlanResponse;
import com.sba301.backend.entity.SubscriptionPlan;
import com.sba301.backend.mapper.SubscriptionPlanMapper;
import com.sba301.backend.repository.SubscriptionPlanRepository;
import com.sba301.backend.service.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPlanMapper planMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionPlanResponse> getAll(Pageable pageable) {
        return planRepository.findAllActive(pageable).map(planMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getById(Long id) {
        return planMapper.toResponse(getActive(id));
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse create(SubscriptionPlanRequest request) {
        if (planRepository.existsByNameAndNotDeleted(request.getName())) {
            throw new AppException(ErrorEnum.SUBSCRIPTION_PLAN_NAME_ALREADY_EXISTS);
        }
        return planMapper.toResponse(planRepository.save(planMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse update(Long id, SubscriptionPlanRequest request) {
        SubscriptionPlan plan = getActive(id);
        if (!plan.getName().equals(request.getName()) && planRepository.existsByNameAndNotDeleted(request.getName())) {
            throw new AppException(ErrorEnum.SUBSCRIPTION_PLAN_NAME_ALREADY_EXISTS);
        }
        planMapper.updateFromRequest(plan, request);
        return planMapper.toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SubscriptionPlan plan = getActive(id);
        plan.setDeletedAt(OffsetDateTime.now());
        planRepository.save(plan);
    }

    private SubscriptionPlan getActive(Long id) {
        return planRepository.findByIdActive(id)
                .orElseThrow(() -> new AppException(ErrorEnum.SUBSCRIPTION_PLAN_NOT_FOUND));
    }
}
