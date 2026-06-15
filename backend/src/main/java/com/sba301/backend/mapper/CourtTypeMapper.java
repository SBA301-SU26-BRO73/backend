package com.sba301.backend.mapper;

import org.springframework.stereotype.Component;

import com.sba301.backend.dto.request.CourtTypeRequest;
import com.sba301.backend.dto.response.CourtTypeResponse;
import com.sba301.backend.entity.CourtType;

@Component
public class CourtTypeMapper {

    public CourtType toEntity(CourtTypeRequest request) {
        CourtType ct = new CourtType();
        ct.setName(request.getName().trim());
        ct.setNameEn(request.getNameEn());
        ct.setDescription(request.getDescription());
        ct.setIcon(request.getIcon());
        ct.setColor(request.getColor());
        ct.setActive(request.isActive());
        return ct;
    }

    public void updateFromRequest(CourtType ct, CourtTypeRequest request) {
        ct.setName(request.getName().trim());
        ct.setNameEn(request.getNameEn());
        ct.setDescription(request.getDescription());
        ct.setIcon(request.getIcon());
        ct.setColor(request.getColor());
        ct.setActive(request.isActive());
    }

    public CourtTypeResponse toResponse(CourtType ct) {
        return CourtTypeResponse.builder()
                .id(ct.getId())
                .name(ct.getName())
                .nameEn(ct.getNameEn())
                .description(ct.getDescription())
                .icon(ct.getIcon())
                .color(ct.getColor())
                .active(ct.isActive())
                .createdAt(ct.getCreatedAt())
                .updatedAt(ct.getUpdatedAt())
                .build();
    }
}
