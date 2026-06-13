package com.sba301.backend.dto.request;

import com.sba301.backend.common.enums.CourtStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourtRequest {

    @NotNull(message = "Branch id is required")
    private Long branchId;

    @NotBlank(message = "Court name is required")
    private String name;

    @NotNull(message = "Court type id is required")
    private Long courtTypeId;

    private String description;
    private String imageUrl;
    private CourtStatus status;
}
