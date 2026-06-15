package com.sba301.backend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.SubscriptionPlanRequest;
import com.sba301.backend.dto.response.SubscriptionPlanResponse;
import com.sba301.backend.entity.SubscriptionPlan;
import com.sba301.backend.mapper.SubscriptionPlanMapper;
import com.sba301.backend.repository.SubscriptionPlanRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceImplTest {

    @Mock private SubscriptionPlanRepository planRepository;
    @Mock private SubscriptionPlanMapper planMapper;

    @InjectMocks
    private SubscriptionPlanServiceImpl subscriptionPlanService;

    private SubscriptionPlan plan;
    private SubscriptionPlanResponse planResponse;
    private SubscriptionPlanRequest planRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        plan = new SubscriptionPlan();
        plan.setId(1L);
        plan.setName("Tiêu chuẩn");
        plan.setMaxBranches(3);
        plan.setMaxCourts(10);

        planResponse = SubscriptionPlanResponse.builder()
                .id(1L).name("Tiêu chuẩn").maxBranches(3).maxCourts(10).build();

        planRequest = new SubscriptionPlanRequest(
                "Tiêu chuẩn", "Gói phổ biến", 699000L, 6990000L, 3, 10,
                List.of("Quản lý đặt sân", "Báo cáo"), "#15803D", false
        );
    }

    @Test
    void getAll_ShouldReturnPage() {
        when(planRepository.findAllActive(pageable)).thenReturn(new PageImpl<>(List.of(plan)));
        when(planMapper.toResponse(any())).thenReturn(planResponse);

        Page<SubscriptionPlanResponse> result = subscriptionPlanService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(planRepository).findAllActive(pageable);
    }

    @Test
    void getById_Success_ShouldReturnResponse() {
        when(planRepository.findByIdActive(1L)).thenReturn(Optional.of(plan));
        when(planMapper.toResponse(plan)).thenReturn(planResponse);

        SubscriptionPlanResponse result = subscriptionPlanService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_NotFound_ShouldThrow() {
        when(planRepository.findByIdActive(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> subscriptionPlanService.getById(99L));
        assertEquals(ErrorEnum.SUBSCRIPTION_PLAN_NOT_FOUND, ex.getErrorEnum());
    }

    @Test
    void create_Success_ShouldReturnResponse() {
        when(planRepository.existsByNameAndNotDeleted("Tiêu chuẩn")).thenReturn(false);
        when(planMapper.toEntity(planRequest)).thenReturn(plan);
        when(planRepository.save(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponse);

        SubscriptionPlanResponse result = subscriptionPlanService.create(planRequest);

        assertNotNull(result);
        verify(planRepository).save(plan);
    }

    @Test
    void create_NameAlreadyExists_ShouldThrow() {
        when(planRepository.existsByNameAndNotDeleted("Tiêu chuẩn")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> subscriptionPlanService.create(planRequest));
        assertEquals(ErrorEnum.SUBSCRIPTION_PLAN_NAME_ALREADY_EXISTS, ex.getErrorEnum());
        verify(planRepository, never()).save(any());
    }

    @Test
    void update_Success_ShouldReturnResponse() {
        plan.setName("Tên cũ");
        when(planRepository.findByIdActive(1L)).thenReturn(Optional.of(plan));
        when(planRepository.existsByNameAndNotDeleted("Tiêu chuẩn")).thenReturn(false);
        when(planRepository.save(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponse);

        SubscriptionPlanResponse result = subscriptionPlanService.update(1L, planRequest);

        assertNotNull(result);
        verify(planMapper).updateFromRequest(plan, planRequest);
    }

    @Test
    void update_SameNameNotDuplicate_ShouldNotThrow() {
        plan.setName("Tiêu chuẩn");
        when(planRepository.findByIdActive(1L)).thenReturn(Optional.of(plan));
        when(planRepository.save(plan)).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponse);

        subscriptionPlanService.update(1L, planRequest);

        verify(planRepository, never()).existsByNameAndNotDeleted(any());
    }

    @Test
    void update_NotFound_ShouldThrow() {
        when(planRepository.findByIdActive(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> subscriptionPlanService.update(99L, planRequest));
        assertEquals(ErrorEnum.SUBSCRIPTION_PLAN_NOT_FOUND, ex.getErrorEnum());
    }

    @Test
    void update_NameAlreadyExists_ShouldThrow() {
        SubscriptionPlan existing = new SubscriptionPlan();
        existing.setId(1L);
        existing.setName("Cũ");
        existing.setFeatures(new ArrayList<>());
        when(planRepository.findByIdActive(1L)).thenReturn(Optional.of(existing));
        when(planRepository.existsByNameAndNotDeleted("Tiêu chuẩn")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> subscriptionPlanService.update(1L, planRequest));
        assertEquals(ErrorEnum.SUBSCRIPTION_PLAN_NAME_ALREADY_EXISTS, ex.getErrorEnum());
    }

    @Test
    void delete_Success_ShouldSetDeletedAt() {
        when(planRepository.findByIdActive(1L)).thenReturn(Optional.of(plan));

        subscriptionPlanService.delete(1L);

        assertNotNull(plan.getDeletedAt());
        verify(planRepository).save(plan);
    }

    @Test
    void delete_NotFound_ShouldThrow() {
        when(planRepository.findByIdActive(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> subscriptionPlanService.delete(99L));
        assertEquals(ErrorEnum.SUBSCRIPTION_PLAN_NOT_FOUND, ex.getErrorEnum());
    }
}
