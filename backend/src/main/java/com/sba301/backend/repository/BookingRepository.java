package com.sba301.backend.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sba301.backend.common.enums.BookingStatus;
import com.sba301.backend.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdAndStatus(Long id, BookingStatus status);

    Page<Booking> findAllByStatus(BookingStatus status, Pageable pageable);

    boolean existsByCourtIdAndDateAndStatus(
            Long courtId,
            LocalDate date,
            BookingStatus status
    );
}