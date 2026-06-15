package com.sba301.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.dto.request.CreateStaffRequest;
import com.sba301.backend.dto.request.UpdateStaffRequest;
import com.sba301.backend.dto.request.WalkInBookingRequest;
import com.sba301.backend.dto.response.StaffCheckinResponse;
import com.sba301.backend.dto.response.StaffCheckoutResponse;
import com.sba301.backend.dto.response.StaffResponse;
import com.sba301.backend.dto.response.StaffScheduleResponse;
import com.sba301.backend.dto.response.WalkInBookingResponse;

public interface StaffService {

    StaffResponse create(CreateStaffRequest request);

    StaffResponse getById(Long id);

    Page<StaffResponse> getByBranch(Long branchId, Pageable pageable);

    StaffResponse update(Long id, UpdateStaffRequest request);

    void delete(Long id);

    List<StaffScheduleResponse> getTodaySchedule(Long staffUserId, LocalDate date);

    StaffCheckinResponse checkIn(Long staffUserId, String checkinCode);

    WalkInBookingResponse createWalkInBooking(WalkInBookingRequest request);

    StaffCheckoutResponse checkout(Long staffUserId, Long bookingId);
}
