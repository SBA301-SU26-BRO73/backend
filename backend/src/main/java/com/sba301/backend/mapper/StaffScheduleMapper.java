package com.sba301.backend.mapper;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sba301.backend.dto.response.StaffCheckinResponse;
import com.sba301.backend.dto.response.StaffCheckoutResponse;
import com.sba301.backend.dto.response.StaffScheduleResponse;
import com.sba301.backend.dto.response.WalkInBookingResponse;
import com.sba301.backend.entity.Booking;
import com.sba301.backend.entity.BookingSlot;
import com.sba301.backend.entity.User;

@Component
public class StaffScheduleMapper {

    public StaffScheduleResponse toScheduleResponse(Booking booking, List<BookingSlot> slots) {
        return StaffScheduleResponse.builder()
                .bookingId(booking.getId())
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getName())
                .customerName(customerName(booking.getCustomer()))
                .guestPhone(booking.getGuestPhone())
                .status(booking.getStatus())
                .date(booking.getDate())
                .startTime(startTime(slots))
                .endTime(endTime(slots))
                .totalPrice(booking.getTotalPrice())
                .slotCount(slots.size())
                .build();
    }

    public StaffCheckinResponse toCheckinResponse(Booking booking, List<BookingSlot> slots) {
        return StaffCheckinResponse.builder()
                .bookingId(booking.getId())
                .courtName(booking.getCourt().getName())
                .customerName(customerName(booking.getCustomer()))
                .guestPhone(booking.getGuestPhone())
                .status(booking.getStatus())
                .date(booking.getDate())
                .startTime(startTime(slots))
                .endTime(endTime(slots))
                .checkedInAt(booking.getCheckedInAt())
                .build();
    }

    public WalkInBookingResponse toWalkInResponse(Booking booking, List<BookingSlot> slots, Long paymentId) {
        return WalkInBookingResponse.builder()
                .bookingId(booking.getId())
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getName())
                .guestPhone(booking.getGuestPhone())
                .date(booking.getDate())
                .startTime(startTime(slots))
                .endTime(endTime(slots))
                .slotCount(slots.size())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .checkinCode(booking.getCheckinCode())
                .checkedInAt(booking.getCheckedInAt())
                .paymentId(paymentId)
                .build();
    }

    public StaffCheckoutResponse toCheckoutResponse(Booking booking) {
        return StaffCheckoutResponse.builder()
                .bookingId(booking.getId())
                .courtName(booking.getCourt().getName())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .completedAt(booking.getCompletedAt())
                .build();
    }

    private String customerName(User customer) {
        return customer == null ? null : customer.getEmail();
    }

    private LocalTime startTime(List<BookingSlot> slots) {
        return slots.stream()
                .map(BookingSlot::getSlotStart)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private LocalTime endTime(List<BookingSlot> slots) {
        return slots.stream()
                .map(BookingSlot::getSlotEnd)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
