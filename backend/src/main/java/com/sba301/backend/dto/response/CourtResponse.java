package com.sba301.backend.dto.response;

import java.time.OffsetDateTime;

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
public class CourtResponse {

    private Long id;
    private Long branchId;
    private String branchName;
    private String name;
    private Long courtTypeId;
    private String courtTypeName;
    private String description;
    private String imageUrl;
    private CourtStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
