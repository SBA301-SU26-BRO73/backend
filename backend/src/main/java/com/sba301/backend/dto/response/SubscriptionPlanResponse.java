package com.sba301.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPlanResponse {

    private Long id;
    private String name;
    private String tagline;
    private Long monthlyPrice;
    private Long yearlyPrice;
    private Integer maxBranches;
    private Integer maxCourts;
    private List<String> features;
    private String color;
    private boolean popular;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
