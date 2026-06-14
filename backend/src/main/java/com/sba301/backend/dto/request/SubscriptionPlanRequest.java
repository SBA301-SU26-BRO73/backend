package com.sba301.backend.dto.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String tagline;

    private Long monthlyPrice;
    private Long yearlyPrice;

    @NotNull
    @Min(1)
    private Integer maxBranches;

    @NotNull
    @Min(1)
    private Integer maxCourts;

    private List<String> features = new ArrayList<>();

    @Size(max = 20)
    private String color;

    private boolean popular = false;
}
