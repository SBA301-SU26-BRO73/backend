package com.sba301.backend.mapper;

import org.springframework.stereotype.Component;

import com.sba301.backend.dto.response.BookingResponse;
import com.sba301.backend.entity.Booking;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {

        return BookingResponse.builder()
                .id(booking.getId())

                .courtId(
                        booking.getCourt() != null
                                ? booking.getCourt().getId()
                                : null)

                .courtName(
                        booking.getCourt() != null
                                ? booking.getCourt().getName()
                                : null)

                .customerId(
                        booking.getCustomer() != null
                                ? booking.getCustomer().getId()
                                : null)

                .customerEmail(
                        booking.getCustomer() != null
                                ? booking.getCustomer().getEmail()
                                : null)

                .guestPhone(booking.getGuestPhone())

                .date(booking.getDate())

                .status(booking.getStatus())

                .totalPrice(booking.getTotalPrice())

                .createdAt(booking.getCreatedAt())

                .updatedAt(booking.getUpdatedAt())

                .build();
    }
}