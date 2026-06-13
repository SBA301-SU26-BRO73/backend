package com.sba301.backend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.sba301.backend.common.enums.BookingStatus;
import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.CreateStaffRequest;
import com.sba301.backend.dto.request.UpdateStaffRequest;
import com.sba301.backend.dto.response.StaffCheckinResponse;
import com.sba301.backend.dto.response.StaffResponse;
import com.sba301.backend.dto.response.StaffScheduleResponse;
import com.sba301.backend.entity.Booking;
import com.sba301.backend.entity.BookingSlot;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.Court;
import com.sba301.backend.entity.Staff;
import com.sba301.backend.entity.User;
import com.sba301.backend.exception.BadRequestException;
import com.sba301.backend.exception.ResourceNotFoundException;
import com.sba301.backend.mapper.StaffMapper;
import com.sba301.backend.mapper.StaffScheduleMapper;
import com.sba301.backend.repository.BookingRepository;
import com.sba301.backend.repository.BookingSlotRepository;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.StaffRepository;
import com.sba301.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

    private static final Long STAFF_USER_ID = 10L;
    private static final Long BRANCH_ID = 1L;
    private static final Long OTHER_BRANCH_ID = 2L;
    private static final Long COURT_ID = 3L;
    private static final Long BOOKING_ID = 100L;
    private static final String CODE = "code-abc";

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private StaffMapper staffMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingSlotRepository bookingSlotRepository;

    @Mock
    private StaffScheduleMapper staffScheduleMapper;

    @InjectMocks
    private StaffServiceImpl staffService;

    private Branch branch;
    private Staff staff;
    private Court court;
    private Booking booking;

    @BeforeEach
    void setUpScheduleCheckinFixtures() {
        branch = new Branch();
        branch.setId(BRANCH_ID);
        branch.setName("Branch 1");

        User user = new User();
        user.setId(STAFF_USER_ID);

        staff = new Staff();
        staff.setId(1L);
        staff.setUser(user);
        staff.setBranch(branch);

        court = new Court();
        court.setId(COURT_ID);
        court.setBranch(branch);
        court.setName("Court 1");

        booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setCourt(court);
        booking.setDate(LocalDate.now(ZoneOffset.UTC));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCheckinCode(CODE);
    }

    private BookingSlot slot(LocalTime start, LocalTime end) {
        BookingSlot s = new BookingSlot();
        s.setBooking(booking);
        s.setCourt(court);
        s.setSlotStart(start);
        s.setSlotEnd(end);
        return s;
    }

    // --- create ---

    @Test
    void create_success() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .email("staff@test.com")
                .password("secret123")
                .phone("0901234567")
                .branchId(1L)
                .build();

        Branch branch = new Branch();
        User savedUser = new User();
        Staff savedStaff = new Staff();
        StaffResponse expected = StaffResponse.builder().id(1L).build();

        when(userRepository.existsByEmailAndDeletedAtIsNull("staff@test.com")).thenReturn(false);
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(passwordEncoder.encode("secret123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(staffRepository.save(any(Staff.class))).thenReturn(savedStaff);
        when(staffMapper.toResponse(savedStaff)).thenReturn(expected);

        StaffResponse result = staffService.create(request);

        assertThat(result).isEqualTo(expected);
        verify(userRepository).save(any(User.class));
        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    void create_emailAlreadyExists_throwsException() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .email("duplicate@test.com")
                .password("secret123")
                .branchId(1L)
                .build();

        when(userRepository.existsByEmailAndDeletedAtIsNull("duplicate@test.com")).thenReturn(true);

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.create(request))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.USER_EMAIL_ALREADY_EXISTS));
    }

    @Test
    void create_branchNotFound_throwsException() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .email("staff@test.com")
                .password("secret123")
                .branchId(99L)
                .build();

        when(userRepository.existsByEmailAndDeletedAtIsNull("staff@test.com")).thenReturn(false);
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(99L, BranchStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.create(request))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.BRANCH_NOT_FOUND));
    }

    // --- getById ---

    @Test
    void getById_found_returnsResponse() {
        Staff staff = new Staff();
        StaffResponse expected = StaffResponse.builder().id(1L).build();

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(staffMapper.toResponse(staff)).thenReturn(expected);

        assertThat(staffService.getById(1L)).isEqualTo(expected);
    }

    @Test
    void getById_notFound_throwsException() {
        when(staffRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.getById(99L))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.STAFF_NOT_FOUND));
    }

    // --- getByBranch ---

    @Test
    void getByBranch_returnsPagedResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Staff staff = new Staff();
        StaffResponse response = StaffResponse.builder().id(1L).build();
        Page<Staff> staffPage = new PageImpl<>(List.of(staff));

        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(new Branch()));
        when(staffRepository.findAllByBranchIdAndDeletedAtIsNull(1L, pageable)).thenReturn(staffPage);
        when(staffMapper.toResponse(staff)).thenReturn(response);

        Page<StaffResponse> result = staffService.getByBranch(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
    }

    @Test
    void getByBranch_branchNotFound_throwsException() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(99L, BranchStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.getByBranch(99L, PageRequest.of(0, 10)))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.BRANCH_NOT_FOUND));
    }

    // --- update ---

    @Test
    void update_success() {
        UpdateStaffRequest request = UpdateStaffRequest.builder()
                .phone("0909999999")
                .branchId(2L)
                .build();

        User user = new User();
        Branch newBranch = new Branch();
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(new Branch());

        Staff savedStaff = new Staff();
        StaffResponse expected = StaffResponse.builder().id(1L).build();

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(2L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(newBranch));
        when(staffRepository.save(staff)).thenReturn(savedStaff);
        when(staffMapper.toResponse(savedStaff)).thenReturn(expected);

        StaffResponse result = staffService.update(1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getPhone()).isEqualTo("0909999999");
        assertThat(staff.getBranch()).isEqualTo(newBranch);
    }

    @Test
    void update_staffNotFound_throwsException() {
        when(staffRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.update(99L, UpdateStaffRequest.builder().build()))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.STAFF_NOT_FOUND));
    }

    @Test
    void update_branchNotFound_throwsException() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().branchId(99L).build();
        User user = new User();
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(new Branch());

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(99L, BranchStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.update(1L, request))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.BRANCH_NOT_FOUND));
    }

    @Test
    void update_onlyPhone_success() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().phone("0911111111").build();
        User user = new User();
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(new Branch());
        StaffResponse expected = StaffResponse.builder().id(1L).build();

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(staffRepository.save(staff)).thenReturn(staff);
        when(staffMapper.toResponse(staff)).thenReturn(expected);

        StaffResponse result = staffService.update(1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getPhone()).isEqualTo("0911111111");
    }

    @Test
    void update_onlyBranchId_success() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().branchId(3L).build();
        User user = new User();
        Branch newBranch = new Branch();
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(new Branch());
        StaffResponse expected = StaffResponse.builder().id(1L).build();

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(3L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(newBranch));
        when(staffRepository.save(staff)).thenReturn(staff);
        when(staffMapper.toResponse(staff)).thenReturn(expected);

        StaffResponse result = staffService.update(1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(staff.getBranch()).isEqualTo(newBranch);
    }

    // --- delete ---

    @Test
    void delete_success() {
        User user = new User();
        Staff staff = new Staff();
        staff.setUser(user);

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));

        staffService.delete(1L);

        assertThat(staff.getDeletedAt()).isNotNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(user);
        verify(staffRepository).save(staff);
    }

    @Test
    void delete_staffNotFound_throwsException() {
        when(staffRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.delete(99L))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.STAFF_NOT_FOUND));
    }

    // --- getTodaySchedule ---

    @Test
    void getTodaySchedule_returnsMappedRows() {
        LocalDate date = LocalDate.of(2026, 6, 20);
        StaffScheduleResponse mapped = StaffScheduleResponse.builder()
                .bookingId(BOOKING_ID).startTime(LocalTime.of(6, 0)).courtName("Court 1").build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(bookingRepository.findScheduleByBranchAndDate(BRANCH_ID, date))
                .thenReturn(List.of(booking));
        when(bookingSlotRepository.findByBooking_IdInOrderByBooking_IdAscSlotStartAsc(anyList()))
                .thenReturn(List.of(slot(LocalTime.of(6, 0), LocalTime.of(6, 30))));
        when(staffScheduleMapper.toScheduleResponse(any(Booking.class), anyList()))
                .thenReturn(mapped);

        List<StaffScheduleResponse> result = staffService.getTodaySchedule(STAFF_USER_ID, date);

        assertEquals(1, result.size());
        assertThat(result.get(0)).isEqualTo(mapped);
    }

    @Test
    void getTodaySchedule_nullDate_defaultsToToday() {
        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(bookingRepository.findScheduleByBranchAndDate(BRANCH_ID, LocalDate.now(ZoneOffset.UTC)))
                .thenReturn(List.of());

        List<StaffScheduleResponse> result = staffService.getTodaySchedule(STAFF_USER_ID, null);

        assertEquals(0, result.size());
        verify(bookingRepository).findScheduleByBranchAndDate(BRANCH_ID, LocalDate.now(ZoneOffset.UTC));
    }

    @Test
    void getTodaySchedule_unknownStaff_throws404() {
        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> staffService.getTodaySchedule(STAFF_USER_ID, null));
    }

    // --- checkIn ---

    @Test
    void checkIn_confirmedBooking_transitionsToCheckedIn() {
        StaffCheckinResponse mapped = StaffCheckinResponse.builder().bookingId(BOOKING_ID).build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(bookingRepository.findByCheckinCode(CODE)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingSlotRepository.findByBooking_IdInOrderByBooking_IdAscSlotStartAsc(anyList()))
                .thenReturn(List.of(slot(LocalTime.of(6, 0), LocalTime.of(6, 30))));
        when(staffScheduleMapper.toCheckinResponse(any(Booking.class), anyList()))
                .thenReturn(mapped);

        StaffCheckinResponse result = staffService.checkIn(STAFF_USER_ID, CODE);

        assertThat(result).isEqualTo(mapped);
        assertEquals(BookingStatus.CHECKED_IN, booking.getStatus());
        assertNotNull(booking.getCheckedInAt());
        verify(bookingRepository).save(booking);
    }

    @Test
    void checkIn_invalidCode_throws404() {
        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(bookingRepository.findByCheckinCode(CODE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> staffService.checkIn(STAFF_USER_ID, CODE));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void checkIn_wrongBranch_throws400() {
        Branch other = new Branch();
        other.setId(OTHER_BRANCH_ID);
        court.setBranch(other);

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(bookingRepository.findByCheckinCode(CODE)).thenReturn(Optional.of(booking));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.checkIn(STAFF_USER_ID, CODE));
        assertEquals("Booking belongs to another branch", ex.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void checkIn_notToday_throws400() {
        booking.setDate(LocalDate.now(ZoneOffset.UTC).plusDays(1));

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(bookingRepository.findByCheckinCode(CODE)).thenReturn(Optional.of(booking));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.checkIn(STAFF_USER_ID, CODE));
        assertEquals("Booking is not scheduled for today", ex.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void checkIn_alreadyCheckedIn_throws400() {
        booking.setStatus(BookingStatus.CHECKED_IN);

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(bookingRepository.findByCheckinCode(CODE)).thenReturn(Optional.of(booking));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.checkIn(STAFF_USER_ID, CODE));
        assertEquals("Booking already checked in", ex.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void checkIn_notConfirmedYet_throws400() {
        booking.setStatus(BookingStatus.AWAITING_CONFIRMATION);

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(bookingRepository.findByCheckinCode(CODE)).thenReturn(Optional.of(booking));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.checkIn(STAFF_USER_ID, CODE));
        assertEquals("Booking is not confirmed yet", ex.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
