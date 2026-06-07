package com.sba301.backend.repository;

import com.sba301.backend.dto.DailySlotDto;
import com.sba301.backend.entity.TimeSlotTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotTemplateRepository extends JpaRepository<TimeSlotTemplate, Long> {

    @Query(value = """
        SELECT t.start_time AS startTime,
               t.end_time AS endTime,
               t.price AS price,
               CASE 
                   -- 1. Khóa nếu ngày tra cứu nằm trong quá khứ, HOẶC là hôm nay nhưng giờ đã trôi qua
                   WHEN CAST(:date AS DATE) < CURRENT_DATE 
                        OR (CAST(:date AS DATE) = CURRENT_DATE AND t.start_time <= CURRENT_TIME) 
                   THEN 'EXPIRED'
                   
                   -- 2. Đã có người đặt
                   WHEN b.id IS NOT NULL THEN 'BOOKED'
                   
                   -- 3. Đang có người giữ chỗ
                   WHEN h.id IS NOT NULL THEN 'HOLDING'
                   
                   -- 4. Trống
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
          -- Đã gỡ bỏ điều kiện AND CAST(:date AS DATE) >= CURRENT_DATE ở đây
          
        ORDER BY t.start_time
    """, nativeQuery = true)
    List<DailySlotDto> getDailyCourtSchedule(@Param("courtId") Long courtId, @Param("date") LocalDate date);
}