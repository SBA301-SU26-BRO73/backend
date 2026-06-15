package com.sba301.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sba301.backend.dto.request.SubscriptionPlanRequest;
import com.sba301.backend.dto.response.SubscriptionPlanResponse;
import com.sba301.backend.entity.PlanFeature;
import com.sba301.backend.entity.SubscriptionPlan;

@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlan toEntity(SubscriptionPlanRequest request) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(request.getName().trim());
        plan.setTagline(request.getTagline());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setMaxBranches(request.getMaxBranches());
        plan.setMaxCourts(request.getMaxCourts());
        plan.setColor(request.getColor());
        plan.setPopular(request.isPopular());
        setFeatures(plan, request.getFeatures());
        return plan;
    }

    public void updateFromRequest(SubscriptionPlan plan, SubscriptionPlanRequest request) {
        plan.setName(request.getName().trim());
        plan.setTagline(request.getTagline());
        plan.setMonthlyPrice(request.getMonthlyPrice());
        plan.setYearlyPrice(request.getYearlyPrice());
        plan.setMaxBranches(request.getMaxBranches());
        plan.setMaxCourts(request.getMaxCourts());
        plan.setColor(request.getColor());
        plan.setPopular(request.isPopular());
        plan.getFeatures().clear();
        setFeatures(plan, request.getFeatures());
    }

    public SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        List<String> features = plan.getFeatures().stream()
                .map(PlanFeature::getFeature)
                .collect(Collectors.toList());

        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .tagline(plan.getTagline())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .maxBranches(plan.getMaxBranches())
                .maxCourts(plan.getMaxCourts())
                .features(features)
                .color(plan.getColor())
                .popular(plan.isPopular())
                .active(plan.isActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

    private void setFeatures(SubscriptionPlan plan, List<String> featureStrings) {
        if (featureStrings == null) return;
        featureStrings.stream()
                .filter(f -> f != null && !f.isBlank())
                .forEach(f -> {
                    PlanFeature pf = new PlanFeature();
                    pf.setPlan(plan);
                    pf.setFeature(f.trim());
                    plan.getFeatures().add(pf);
                });
    }
}
