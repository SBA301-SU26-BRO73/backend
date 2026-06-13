package com.sba301.backend.dto.request;

import com.sba301.backend.common.enums.CourtStatus;

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
public class UpdateCourtRequest {

    private String name;
    private Long courtTypeId;
    private String description;
    private String imageUrl;
    private CourtStatus status;
}
