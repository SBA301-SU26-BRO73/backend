package com.sba301.backend.service.impl;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.UpdateBookingStatusRequest;
import com.sba301.backend.dto.response.BookingResponse;
import com.sba301.backend.entity.Booking;
import com.sba301.backend.mapper.BookingMapper;
import com.sba301.backend.repository.BookingRepository;
import com.sba301.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable)
                .map(bookingMapper::toResponse);
    }

    @Override
    @Transactional
    public BookingResponse updateStatus(Long id, UpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorEnum.BOOKING_NOT_FOUND));

        booking.setStatus(request.getStatus());

        return bookingMapper.toResponse(bookingRepository.save(booking));
    }
}
