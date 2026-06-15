package com.sba301.backend.repository;

import java.time.LocalTime;
import com.sba301.backend.repository.projection.DailySlotProjection;
import com.sba301.backend.entity.TimeSlotTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
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

    Optional<TimeSlotTemplate> findByCourtIdAndDayOfWeekAndStartTimeAndActiveTrueAndDeletedAtIsNull(
            Long courtId,
            Short dayOfWeek,
            LocalTime startTime);

    @Query(value = """
        SELECT t.start_time AS startTime,
               t.end_time AS endTime,
               t.price AS price,
               CASE 
                   WHEN CAST(:date AS DATE) < CURRENT_DATE 
                        OR (CAST(:date AS DATE) = CURRENT_DATE AND t.start_time <= CURRENT_TIME) 
                   THEN 'EXPIRED'
                   WHEN b.id IS NOT NULL THEN 'BOOKED'
                   WHEN h.id IS NOT NULL THEN 'HOLDING'
                   ELSE 'AVAILABLE'
               END AS status
        FROM time_slot_templates t
        LEFT JOIN booking_slots b 
               ON b.court_id = t.court_id 
              AND b.booking_date = CAST(:date AS DATE) 
              AND b.slot_start = t.start_time
        LEFT JOIN slot_holds h 
               ON h.court_id = t.court_id 
              AND h.date = CAST(:date AS DATE) 
              AND h.slot_start = t.start_time 
              AND h.expired_at > CURRENT_TIMESTAMP
        WHERE t.court_id = :courtId
          AND t.day_of_week = EXTRACT(DOW FROM CAST(:date AS DATE))
          AND t.is_active = TRUE
          AND t.deleted_at IS NULL
        ORDER BY t.start_time
    """, nativeQuery = true)
    List<DailySlotProjection> getDailyCourtSchedule(@Param("courtId") Long courtId, @Param("date") LocalDate date);
}
