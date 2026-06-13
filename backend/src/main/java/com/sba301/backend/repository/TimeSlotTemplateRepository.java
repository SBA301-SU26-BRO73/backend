package com.sba301.backend.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sba301.backend.entity.TimeSlotTemplate;

public interface TimeSlotTemplateRepository extends JpaRepository<TimeSlotTemplate, Long> {

    Optional<TimeSlotTemplate> findByIdAndDeletedAtIsNull(Long id);

    Page<TimeSlotTemplate> findAllByDeletedAtIsNull(Pageable pageable);

    List<TimeSlotTemplate> findByCourtIdAndDeletedAtIsNull(Long courtId);

    boolean existsByCourtIdAndDayOfWeekAndStartTimeAndEndTimeAndDeletedAtIsNull(
            Long courtId,
            Short dayOfWeek,
            LocalTime startTime,
            LocalTime endTime);

    boolean existsByCourtIdAndDayOfWeekAndStartTimeAndEndTimeAndDeletedAtIsNullAndIdNot(
            Long courtId,
            Short dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Long id);
}
