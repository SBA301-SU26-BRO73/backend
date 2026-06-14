package com.sba301.backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sba301.backend.entity.BookingSlot;

public interface BookingSlotRepository extends JpaRepository<BookingSlot, Long> {

    List<BookingSlot> findByBooking_IdInOrderByBooking_IdAscSlotStartAsc(Collection<Long> bookingIds);

    boolean existsByCourt_IdAndBookingDateAndSlotStart(Long courtId, LocalDate bookingDate, LocalTime slotStart);
}
