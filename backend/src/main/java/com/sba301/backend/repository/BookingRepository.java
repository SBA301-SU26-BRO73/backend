package com.sba301.backend.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sba301.backend.common.enums.BookingStatus;
import com.sba301.backend.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByIdIn(Collection<Long> ids);
    @Query("""
            select b from Booking b
            join fetch b.court c
            where c.branch.id = :branchId
              and b.date = :date
              and b.status <> com.sba301.backend.common.enums.BookingStatus.CANCELLED
            order by b.id""")
    List<Booking> findScheduleByBranchAndDate(
            @Param("branchId") Long branchId, @Param("date") LocalDate date);

    Optional<Booking> findByCheckinCode(String checkinCode);

    Optional<Booking> findByIdAndStatus(Long id, BookingStatus status);

    Page<Booking> findAllByStatus(BookingStatus status, Pageable pageable);

    boolean existsByCourtIdAndDateAndStatus(
            Long courtId,
            LocalDate date,
            BookingStatus status
    );
}
