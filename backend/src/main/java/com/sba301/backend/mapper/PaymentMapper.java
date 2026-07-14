package com.sba301.backend.mapper;

import org.springframework.stereotype.Component;

import com.sba301.backend.dto.response.PaymentBookingResponse;
import com.sba301.backend.dto.response.PaymentResponse;
import com.sba301.backend.entity.Booking;
import com.sba301.backend.entity.Payment;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .branchId(payment.getBranch().getId())
                .branchName(payment.getBranch().getName())
                .amount(payment.getAmount())
                .billImageUrl(payment.getBillImageUrl())
                .status(payment.getStatus())
                .confirmedAt(payment.getConfirmedAt())
                .confirmedById(payment.getConfirmedBy() == null ? null : payment.getConfirmedBy().getId())
                .bookings(payment.getBookings().stream().map(this::toBookingResponse).toList())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private PaymentBookingResponse toBookingResponse(Booking booking) {
        return PaymentBookingResponse.builder()
                .id(booking.getId())
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getName())
                .date(booking.getDate())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .build();
    }
}
