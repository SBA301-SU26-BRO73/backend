package com.sba301.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.dto.request.ApplyTimeSlotTemplateRequest;
import com.sba301.backend.dto.request.CreateTimeSlotTemplateRequest;
import com.sba301.backend.dto.request.UpdateTimeSlotTemplateRequest;
import com.sba301.backend.dto.response.ApplyTimeSlotTemplateResponse;
import com.sba301.backend.dto.response.TimeSlotTemplateResponse;

/**
 * Manages reusable court time slot templates and copies templates between courts.
 */
public interface TimeSlotTemplateService {

    /** Creates a time slot template for a court. */
    TimeSlotTemplateResponse create(CreateTimeSlotTemplateRequest request);

    /** Returns a non-deleted time slot template by id. */
    TimeSlotTemplateResponse getById(Long id);

    /** Returns a page of non-deleted time slot templates. */
    Page<TimeSlotTemplateResponse> getAll(Pageable pageable);

    /** Returns all non-deleted templates assigned to a court. */
    List<TimeSlotTemplateResponse> getByCourt(Long courtId);

    /** Updates the editable fields of a time slot template. */
    TimeSlotTemplateResponse update(Long id, UpdateTimeSlotTemplateRequest request);

    /** Soft deletes a time slot template. */
    void delete(Long id);

    /** Copies missing templates from the source court to the target court. */
    ApplyTimeSlotTemplateResponse applyToCourt(Long targetCourtId, ApplyTimeSlotTemplateRequest request);
}
