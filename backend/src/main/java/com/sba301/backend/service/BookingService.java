package com.sba301.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.dto.request.UpdateBookingStatusRequest;
import com.sba301.backend.dto.response.BookingResponse;

public interface BookingService {

    Page<BookingResponse> getAllBookings(Pageable pageable);

    BookingResponse updateStatus(Long id, UpdateBookingStatusRequest request);

}
