package com.sba301.backend.mapper;
import com.sba301.backend.dto.response.DailySlotResponse;
import com.sba301.backend.repository.projection.DailySlotProjection;
import org.springframework.stereotype.Component;

import com.sba301.backend.dto.request.CreateCourtRequest;
import com.sba301.backend.dto.response.CourtResponse;
import com.sba301.backend.entity.Court;

@Component
public class CourtMapper {

    public Court toEntity(CreateCourtRequest request) {
        Court court = new Court();
        court.setName(request.getName().trim());
        court.setDescription(request.getDescription());
        court.setImageUrl(request.getImageUrl());
        court.setStatus(request.getStatus());
        return court;
    }

    public CourtResponse toResponse(Court court) {
        return CourtResponse.builder()
                .id(court.getId())
                .branchId(court.getBranch().getId())
                .branchName(court.getBranch().getName())
                .name(court.getName())
                .courtTypeId(court.getCourtType().getId())
                .courtTypeName(court.getCourtType().getName())
                .description(court.getDescription())
                .imageUrl(court.getImageUrl())
                .status(court.getStatus())
                .createdAt(court.getCreatedAt())
                .updatedAt(court.getUpdatedAt())
                .build();
    }

    public DailySlotResponse toDailySlotResponse(DailySlotProjection slot) {
        return DailySlotResponse.builder()
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .price(slot.getPrice())
                .status(slot.getStatus())
                .build();
    }
}
