package com.sba301.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sba301.backend.dto.request.ApplyTimeSlotTemplateRequest;
import com.sba301.backend.dto.request.CreateTimeSlotTemplateRequest;
import com.sba301.backend.dto.request.UpdateTimeSlotTemplateRequest;
import com.sba301.backend.dto.response.ApplyTimeSlotTemplateResponse;
import com.sba301.backend.dto.response.TimeSlotTemplateResponse;
import com.sba301.backend.entity.Court;
import com.sba301.backend.entity.TimeSlotTemplate;
import com.sba301.backend.exception.BadRequestException;
import com.sba301.backend.exception.ResourceNotFoundException;
import com.sba301.backend.mapper.TimeSlotTemplateMapper;
import com.sba301.backend.repository.CourtRepository;
import com.sba301.backend.repository.TimeSlotTemplateRepository;
import com.sba301.backend.service.TimeSlotTemplateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeSlotTemplateServiceImpl implements TimeSlotTemplateService {

    private final TimeSlotTemplateRepository timeSlotTemplateRepository;
    private final CourtRepository courtRepository;
    private final TimeSlotTemplateMapper timeSlotTemplateMapper;

    @Override
    @Transactional
    public TimeSlotTemplateResponse create(CreateTimeSlotTemplateRequest request) {
        Court court = getCourt(request.getCourtId());
        validateTemplateValues(
                request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), request.getPrice());
        validateUniqueTemplate(
                court.getId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        TimeSlotTemplate template = timeSlotTemplateMapper.toEntity(request);
        template.setCourt(court);
        return timeSlotTemplateMapper.toResponse(timeSlotTemplateRepository.save(template));
    }

    @Override
    public TimeSlotTemplateResponse getById(Long id) {
        return timeSlotTemplateMapper.toResponse(getTemplate(id));
    }

    @Override
    public Page<TimeSlotTemplateResponse> getAll(Pageable pageable) {
        return timeSlotTemplateRepository.findAllByDeletedAtIsNull(pageable)
                .map(timeSlotTemplateMapper::toResponse);
    }

    @Override
    public List<TimeSlotTemplateResponse> getByCourt(Long courtId) {
        getCourt(courtId);
        return timeSlotTemplateRepository.findByCourtIdAndDeletedAtIsNull(courtId).stream()
                .map(timeSlotTemplateMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TimeSlotTemplateResponse update(Long id, UpdateTimeSlotTemplateRequest request) {
        TimeSlotTemplate template = getTemplate(id);

        LocalTime startTime = request.getStartTime() == null ? template.getStartTime() : request.getStartTime();
        LocalTime endTime = request.getEndTime() == null ? template.getEndTime() : request.getEndTime();
        BigDecimal price = request.getPrice() == null ? template.getPrice() : request.getPrice();
        Short dayOfWeek = request.getDayOfWeek() == null ? template.getDayOfWeek() : request.getDayOfWeek();

        validateTemplateValues(dayOfWeek, startTime, endTime, price);
        validateUniqueTemplate(template.getCourt().getId(), dayOfWeek, startTime, endTime, template.getId());

        template.setStartTime(startTime);
        template.setEndTime(endTime);
        template.setPrice(price);
        template.setDayOfWeek(dayOfWeek);
        if (request.getActive() != null) {
            template.setActive(request.getActive());
        }

        return timeSlotTemplateMapper.toResponse(timeSlotTemplateRepository.save(template));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TimeSlotTemplate template = getTemplate(id);
        template.setActive(false);
        template.setDeletedAt(OffsetDateTime.now());
        timeSlotTemplateRepository.save(template);
    }

    @Override
    @Transactional
    public ApplyTimeSlotTemplateResponse applyToCourt(
            Long targetCourtId, ApplyTimeSlotTemplateRequest request) {
        Court sourceCourt = getCourt(request.getSourceCourtId());
        Court targetCourt = getCourt(targetCourtId);
        List<TimeSlotTemplate> templatesToCopy = new ArrayList<>();
        Set<TemplateKey> targetKeys = new HashSet<>();

        for (TimeSlotTemplate targetTemplate
                : timeSlotTemplateRepository.findByCourtIdAndDeletedAtIsNull(targetCourt.getId())) {
            targetKeys.add(TemplateKey.from(targetTemplate));
        }

        for (TimeSlotTemplate sourceTemplate
                : timeSlotTemplateRepository.findByCourtIdAndDeletedAtIsNull(sourceCourt.getId())) {
            if (targetKeys.add(TemplateKey.from(sourceTemplate))) {
                templatesToCopy.add(copyTemplate(sourceTemplate, targetCourt));
            }
        }

        timeSlotTemplateRepository.saveAll(templatesToCopy);
        return ApplyTimeSlotTemplateResponse.builder()
                .targetCourtId(targetCourt.getId())
                .sourceCourtId(sourceCourt.getId())
                .copiedCount(templatesToCopy.size())
                .build();
    }

    private TimeSlotTemplate copyTemplate(TimeSlotTemplate source, Court targetCourt) {
        TimeSlotTemplate copy = new TimeSlotTemplate();
        copy.setCourt(targetCourt);
        copy.setStartTime(source.getStartTime());
        copy.setEndTime(source.getEndTime());
        copy.setPrice(source.getPrice());
        copy.setDayOfWeek(source.getDayOfWeek());
        copy.setActive(source.isActive());
        return copy;
    }

    private Court getCourt(Long id) {
        return courtRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + id));
    }

    private TimeSlotTemplate getTemplate(Long id) {
        return timeSlotTemplateRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Time slot template not found with id: " + id));
    }

    private void validateTemplateValues(
            Short dayOfWeek, LocalTime startTime, LocalTime endTime, BigDecimal price) {
        if (dayOfWeek == null || dayOfWeek < 0 || dayOfWeek > 6) {
            throw new BadRequestException("Day of week must be between 0 and 6");
        }
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new BadRequestException("Start time must be before end time");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than 0");
        }
    }

    private void validateUniqueTemplate(
            Long courtId, Short dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (timeSlotTemplateRepository
                .existsByCourtIdAndDayOfWeekAndStartTimeAndEndTimeAndDeletedAtIsNull(
                        courtId, dayOfWeek, startTime, endTime)) {
            throw new BadRequestException("Time slot template already exists for this court");
        }
    }

    private void validateUniqueTemplate(
            Long courtId, Short dayOfWeek, LocalTime startTime, LocalTime endTime, Long templateId) {
        if (timeSlotTemplateRepository
                .existsByCourtIdAndDayOfWeekAndStartTimeAndEndTimeAndDeletedAtIsNullAndIdNot(
                        courtId, dayOfWeek, startTime, endTime, templateId)) {
            throw new BadRequestException("Time slot template already exists for this court");
        }
    }

    private record TemplateKey(Short dayOfWeek, LocalTime startTime, LocalTime endTime) {

        private static TemplateKey from(TimeSlotTemplate template) {
            return new TemplateKey(
                    template.getDayOfWeek(), template.getStartTime(), template.getEndTime());
        }
    }
}
