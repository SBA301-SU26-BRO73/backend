package com.sba301.backend.mapper;

import org.springframework.stereotype.Component;

import com.sba301.backend.dto.request.CreateTimeSlotTemplateRequest;
import com.sba301.backend.dto.response.TimeSlotTemplateResponse;
import com.sba301.backend.entity.TimeSlotTemplate;

@Component
public class TimeSlotTemplateMapper {

    public TimeSlotTemplate toEntity(CreateTimeSlotTemplateRequest request) {
        TimeSlotTemplate template = new TimeSlotTemplate();
        template.setStartTime(request.getStartTime());
        template.setEndTime(request.getEndTime());
        template.setPrice(request.getPrice());
        template.setDayOfWeek(request.getDayOfWeek());
        template.setActive(request.getActive() == null || request.getActive());
        return template;
    }

    public TimeSlotTemplateResponse toResponse(TimeSlotTemplate template) {
        return TimeSlotTemplateResponse.builder()
                .id(template.getId())
                .courtId(template.getCourt().getId())
                .courtName(template.getCourt().getName())
                .startTime(template.getStartTime())
                .endTime(template.getEndTime())
                .price(template.getPrice())
                .dayOfWeek(template.getDayOfWeek())
                .active(template.isActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
