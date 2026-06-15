package com.sba301.backend.dto.response;

import java.time.OffsetDateTime;

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
public class CourtTypeResponse {

    private Long id;
    private String name;
    private String nameEn;
    private String description;
    private String icon;
    private String color;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
