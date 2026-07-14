package com.sba301.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sba301.backend.common.enums.BookingStatus;
import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.common.enums.CourtStatus;
import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.PaymentStatus;
import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.CreateStaffRequest;
import com.sba301.backend.dto.request.UpdateStaffRequest;
import com.sba301.backend.dto.request.WalkInBookingRequest;
import com.sba301.backend.dto.response.StaffCheckinResponse;
import com.sba301.backend.dto.response.StaffCheckoutResponse;
import com.sba301.backend.dto.response.StaffResponse;
import com.sba301.backend.dto.response.StaffScheduleResponse;
import com.sba301.backend.dto.response.WalkInBookingResponse;
import com.sba301.backend.entity.Booking;
import com.sba301.backend.entity.BookingSlot;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.Court;
import com.sba301.backend.entity.Payment;
import com.sba301.backend.entity.Staff;
import com.sba301.backend.entity.TimeSlotTemplate;
import com.sba301.backend.entity.User;
import com.sba301.backend.exception.BadRequestException;
import com.sba301.backend.exception.ResourceNotFoundException;
import com.sba301.backend.mapper.StaffMapper;
import com.sba301.backend.mapper.StaffScheduleMapper;
import com.sba301.backend.repository.BookingRepository;
import com.sba301.backend.repository.BookingSlotRepository;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.CourtRepository;
import com.sba301.backend.repository.PaymentRepository;
import com.sba301.backend.repository.StaffRepository;
import com.sba301.backend.repository.TimeSlotTemplateRepository;
import com.sba301.backend.repository.UserRepository;
import com.sba301.backend.service.StaffService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final StaffMapper staffMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BookingRepository bookingRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final StaffScheduleMapper staffScheduleMapper;
    private final CourtRepository courtRepository;
    private final PaymentRepository paymentRepository;
    private final TimeSlotTemplateRepository timeSlotTemplateRepository;

    @Override
    @Transactional
    public StaffResponse create(Long adminUserId, CreateStaffRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new AppException(ErrorEnum.USER_EMAIL_ALREADY_EXISTS);
        }

        Branch branch = getBranch(request.getBranchId());
        assertOwnsBranch(branch, adminUserId);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(UserRole.STAFF);
        user.setStatus(UserStatus.ACTIVE);
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        userRepository.save(user);

        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(branch);

        return staffMapper.toResponse(staffRepository.save(staff));
    }

    @Override
    public StaffResponse getById(Long adminUserId, Long id) {
        Staff staff = getStaff(id);
        assertOwnsBranch(staff.getBranch(), adminUserId);
        return staffMapper.toResponse(staff);
    }

    @Override
    public Page<StaffResponse> getByBranch(Long adminUserId, Long branchId, Pageable pageable) {
        Branch branch = getBranch(branchId);
        assertOwnsBranch(branch, adminUserId);
        return staffRepository.findAllByBranchIdAndDeletedAtIsNull(branchId, pageable)
                .map(staffMapper::toResponse);
    }

    @Override
    @Transactional
    public StaffResponse update(Long adminUserId, Long id, UpdateStaffRequest request) {
        Staff staff = getStaff(id);
        assertOwnsBranch(staff.getBranch(), adminUserId);

        if (request.getBranchId() != null) {
            Branch target = getBranch(request.getBranchId());
            assertOwnsBranch(target, adminUserId);
            staff.setBranch(target);
        }

        if (request.getPhone() != null) {
            staff.getUser().setPhone(request.getPhone());
        }
        if (request.getFullName() != null) {
            staff.getUser().setFullName(request.getFullName());
        }
        if (request.getPassword() != null) {
            staff.getUser().setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return staffMapper.toResponse(staffRepository.save(staff));
    }

    @Override
    @Transactional
    public void delete(Long adminUserId, Long id) {
        Staff staff = getStaff(id);
        assertOwnsBranch(staff.getBranch(), adminUserId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        staff.setDeletedAt(now);
        staff.getUser().setStatus(UserStatus.INACTIVE);
        staff.getUser().setDeletedAt(now.toLocalDateTime());
        userRepository.save(staff.getUser());
        staffRepository.save(staff);
    }

    @Override
    public List<StaffScheduleResponse> getTodaySchedule(Long staffUserId, LocalDate date) {
        Staff staff = resolveStaff(staffUserId);
        LocalDate target = date != null ? date : LocalDate.now(ZoneOffset.UTC);

        List<Booking> bookings =
                bookingRepository.findScheduleByBranchAndDate(staff.getBranch().getId(), target);
        if (bookings.isEmpty()) {
            return List.of();
        }

        Map<Long, List<BookingSlot>> slotsByBooking = loadSlots(bookings);

        return bookings.stream()
                .map(booking -> staffScheduleMapper.toScheduleResponse(
                        booking, slotsByBooking.getOrDefault(booking.getId(), List.of())))
                .sorted(Comparator
                        .comparing(StaffScheduleResponse::getStartTime,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(StaffScheduleResponse::getCourtName,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    @Transactional
    public StaffCheckinResponse checkIn(Long staffUserId, String checkinCode) {
        Staff staff = resolveStaff(staffUserId);

        Booking booking = bookingRepository.findByCheckinCode(checkinCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid check-in code"));

        if (!booking.getCourt().getBranch().getId().equals(staff.getBranch().getId())) {
            throw new BadRequestException("Booking belongs to another branch");
        }

        if (!booking.getDate().equals(LocalDate.now(ZoneOffset.UTC))) {
            throw new BadRequestException("Booking is not scheduled for today");
        }

        validateCheckable(booking.getStatus());

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(OffsetDateTime.now());
        Booking saved = bookingRepository.save(booking);

        List<BookingSlot> slots =
                bookingSlotRepository.findByBooking_IdInOrderByBooking_IdAscSlotStartAsc(
                        List.of(saved.getId()));

        return staffScheduleMapper.toCheckinResponse(saved, slots);
    }

    @Override
    @Transactional
    public WalkInBookingResponse createWalkInBooking(WalkInBookingRequest request) {
        Staff staff = resolveStaff(request.getStaffUserId());

        Court court = courtRepository.findByIdAndDeletedAtIsNull(request.getCourtId())
                .orElseThrow(() -> new ResourceNotFoundException("Court not found: " + request.getCourtId()));
        if (court.getStatus() != CourtStatus.ACTIVE) {
            throw new BadRequestException("Court is not active");
        }
        if (!court.getBranch().getId().equals(staff.getBranch().getId())) {
            throw new BadRequestException("Court belongs to another branch");
        }

        LocalDate date = LocalDate.now(ZoneOffset.UTC);
        // Java DayOfWeek: MON=1..SUN=7 → DB: SUN=0..SAT=6 via value % 7
        short dow = (short) (date.getDayOfWeek().getValue() % 7);

        List<LocalTime> slotStarts = request.getSlotStarts();
        if (slotStarts.stream().distinct().count() != slotStarts.size()) {
            throw new BadRequestException("Duplicate slot starts in request");
        }

        List<BigDecimal> prices = new ArrayList<>();
        List<TimeSlotTemplate> templates = new ArrayList<>();
        for (LocalTime slotStart : slotStarts) {
            if (bookingSlotRepository.existsByCourt_IdAndBookingDateAndSlotStart(
                    request.getCourtId(), date, slotStart)) {
                throw new BadRequestException("Slot already booked: " + slotStart);
            }
            TimeSlotTemplate template = timeSlotTemplateRepository
                    .findByCourtIdAndDayOfWeekAndStartTimeAndActiveTrueAndDeletedAtIsNull(
                            request.getCourtId(), dow, slotStart)
                    .orElseThrow(() -> new BadRequestException("No price configured for slot: " + slotStart));
            prices.add(template.getPrice());
            templates.add(template);
        }

        // A booking must cover consecutive slots: each slot's end == next slot's start.
        List<TimeSlotTemplate> ordered = templates.stream()
                .sorted(Comparator.comparing(TimeSlotTemplate::getStartTime))
                .toList();
        for (int i = 0; i < ordered.size() - 1; i++) {
            if (!ordered.get(i).getEndTime().equals(ordered.get(i + 1).getStartTime())) {
                throw new BadRequestException("Slots must be consecutive");
            }
        }

        BigDecimal totalPrice = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        OffsetDateTime now = OffsetDateTime.now();

        Payment payment = new Payment();
        payment.setBranch(staff.getBranch());
        payment.setAmount(totalPrice);
        payment.setStatus(PaymentStatus.CONFIRMED);
        payment.setConfirmedAt(now);
        payment.setConfirmedBy(staff.getUser());
        Payment savedPayment = paymentRepository.save(payment);

        Booking booking = new Booking();
        booking.setCourt(court);
        booking.setGuestPhone(request.getGuestPhone());
        booking.setPayment(savedPayment);
        booking.setDate(date);
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setTotalPrice(totalPrice);
        booking.setCheckinCode(UUID.randomUUID().toString());
        booking.setCheckedInAt(now);
        Booking savedBooking = bookingRepository.save(booking);

        List<BookingSlot> slots = new ArrayList<>();
        for (int i = 0; i < slotStarts.size(); i++) {
            BookingSlot slot = new BookingSlot();
            slot.setBooking(savedBooking);
            slot.setCourt(court);
            slot.setBookingDate(date);
            slot.setSlotStart(slotStarts.get(i));
            slot.setSlotEnd(slotStarts.get(i).plusMinutes(30));
            slot.setPriceAtBooking(prices.get(i));
            slots.add(slot);
        }
        try {
            bookingSlotRepository.saveAll(slots);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Slot already booked");
        }

        List<BookingSlot> savedSlots = bookingSlotRepository
                .findByBooking_IdInOrderByBooking_IdAscSlotStartAsc(List.of(savedBooking.getId()));

        return staffScheduleMapper.toWalkInResponse(savedBooking, savedSlots, savedPayment.getId());
    }

    @Override
    @Transactional
    public StaffCheckoutResponse checkout(Long staffUserId, Long bookingId) {
        Staff staff = resolveStaff(staffUserId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getCourt().getBranch().getId().equals(staff.getBranch().getId())) {
            throw new BadRequestException("Booking belongs to another branch");
        }

        validateCompletable(booking.getStatus());

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(OffsetDateTime.now());
        Booking saved = bookingRepository.save(booking);

        return staffScheduleMapper.toCheckoutResponse(saved);
    }

    private void validateCheckable(BookingStatus status) {
        switch (status) {
            case CONFIRMED -> { /* the only checkable state */ }
            case CHECKED_IN -> throw new BadRequestException("Booking already checked in");
            case COMPLETED -> throw new BadRequestException("Booking already completed");
            case CANCELLED -> throw new BadRequestException("Booking has been cancelled");
            case PENDING_PAYMENT, AWAITING_CONFIRMATION ->
                    throw new BadRequestException("Booking is not confirmed yet");
        }
    }

    private void validateCompletable(BookingStatus status) {
        switch (status) {
            case CHECKED_IN -> { /* the only completable state */ }
            case COMPLETED -> throw new BadRequestException("Booking already completed");
            case CONFIRMED -> throw new BadRequestException("Booking has not been checked in yet");
            case CANCELLED -> throw new BadRequestException("Booking has been cancelled");
            case PENDING_PAYMENT, AWAITING_CONFIRMATION ->
                    throw new BadRequestException("Booking is not confirmed yet");
        }
    }

    private Map<Long, List<BookingSlot>> loadSlots(List<Booking> bookings) {
        List<Long> ids = bookings.stream().map(Booking::getId).toList();
        return bookingSlotRepository.findByBooking_IdInOrderByBooking_IdAscSlotStartAsc(ids)
                .stream()
                .collect(Collectors.groupingBy(slot -> slot.getBooking().getId()));
    }

    private Staff resolveStaff(Long staffUserId) {
        return staffRepository.findByUser_IdAndDeletedAtIsNull(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff not found for user id: " + staffUserId));
    }

    private Staff getStaff(Long id) {
        return staffRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorEnum.STAFF_NOT_FOUND));
    }

    private Branch getBranch(Long branchId) {
        return branchRepository.findByIdAndStatusAndDeletedAtIsNull(branchId, BranchStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorEnum.BRANCH_NOT_FOUND));
    }

    // Admins may only manage staff of branches they own (branches.admin_id).
    private void assertOwnsBranch(Branch branch, Long adminUserId) {
        if (!branch.getAdmin().getId().equals(adminUserId)) {
            throw new AppException(ErrorEnum.ACCESS_DENIED);
        }
    }
}
