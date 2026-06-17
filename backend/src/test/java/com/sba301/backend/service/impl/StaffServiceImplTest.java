package com.sba301.backend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
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

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

    private static final Long STAFF_USER_ID = 10L;
    private static final Long ADMIN_USER_ID = 50L;
    private static final Long OTHER_ADMIN_ID = 51L;
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

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TimeSlotTemplateRepository timeSlotTemplateRepository;

    @InjectMocks
    private StaffServiceImpl staffService;

    private Branch branch;
    private Staff staff;
    private Court court;
    private Booking booking;

    @BeforeEach
    void setUpScheduleCheckinFixtures() {
        branch = branchOwnedBy(ADMIN_USER_ID);
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

    private Branch branchOwnedBy(Long ownerUserId) {
        Branch b = new Branch();
        User admin = new User();
        admin.setId(ownerUserId);
        b.setAdmin(admin);
        return b;
    }

    private TimeSlotTemplate template(LocalTime start, LocalTime end, String price) {
        TimeSlotTemplate t = new TimeSlotTemplate();
        t.setStartTime(start);
        t.setEndTime(end);
        t.setPrice(new BigDecimal(price));
        return t;
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

        Branch branch = branchOwnedBy(ADMIN_USER_ID);
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

        StaffResponse result = staffService.create(ADMIN_USER_ID, request);

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
                .isThrownBy(() -> staffService.create(ADMIN_USER_ID, request))
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
                .isThrownBy(() -> staffService.create(ADMIN_USER_ID, request))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.BRANCH_NOT_FOUND));
    }

    @Test
    void create_withFullName_setsFullNameOnUser() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .email("staff@test.com").password("secret123")
                .fullName("Nguyen Van A").branchId(1L).build();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.existsByEmailAndDeletedAtIsNull("staff@test.com")).thenReturn(false);
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branchOwnedBy(ADMIN_USER_ID)));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(staffRepository.save(any(Staff.class))).thenAnswer(inv -> inv.getArgument(0));
        when(staffMapper.toResponse(any())).thenReturn(StaffResponse.builder().id(1L).build());

        staffService.create(ADMIN_USER_ID, request);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFullName()).isEqualTo("Nguyen Van A");
    }

    @Test
    void create_branchNotOwned_throwsAccessDenied() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .email("staff@test.com").password("secret123").branchId(1L).build();

        when(userRepository.existsByEmailAndDeletedAtIsNull("staff@test.com")).thenReturn(false);
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branchOwnedBy(OTHER_ADMIN_ID)));

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.create(ADMIN_USER_ID, request))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.ACCESS_DENIED));
        verify(userRepository, never()).save(any(User.class));
        verify(staffRepository, never()).save(any(Staff.class));
    }

    // --- getById ---

    @Test
    void getById_found_returnsResponse() {
        Staff staff = new Staff();
        staff.setBranch(branchOwnedBy(ADMIN_USER_ID));
        StaffResponse expected = StaffResponse.builder().id(1L).build();

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(staffMapper.toResponse(staff)).thenReturn(expected);

        assertThat(staffService.getById(ADMIN_USER_ID, 1L)).isEqualTo(expected);
    }

    @Test
    void getById_notFound_throwsException() {
        when(staffRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.getById(ADMIN_USER_ID, 99L))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.STAFF_NOT_FOUND));
    }

    @Test
    void getById_branchNotOwned_throwsAccessDenied() {
        Staff staff = new Staff();
        staff.setBranch(branchOwnedBy(OTHER_ADMIN_ID));

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.getById(ADMIN_USER_ID, 1L))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.ACCESS_DENIED));
        verify(staffMapper, never()).toResponse(any());
    }

    // --- getByBranch ---

    @Test
    void getByBranch_returnsPagedResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Staff staff = new Staff();
        StaffResponse response = StaffResponse.builder().id(1L).build();
        Page<Staff> staffPage = new PageImpl<>(List.of(staff));

        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branchOwnedBy(ADMIN_USER_ID)));
        when(staffRepository.findAllByBranchIdAndDeletedAtIsNull(1L, pageable)).thenReturn(staffPage);
        when(staffMapper.toResponse(staff)).thenReturn(response);

        Page<StaffResponse> result = staffService.getByBranch(ADMIN_USER_ID, 1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
    }

    @Test
    void getByBranch_branchNotFound_throwsException() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(99L, BranchStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.getByBranch(ADMIN_USER_ID, 99L, PageRequest.of(0, 10)))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.BRANCH_NOT_FOUND));
    }

    @Test
    void getByBranch_branchNotOwned_throwsAccessDenied() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branchOwnedBy(OTHER_ADMIN_ID)));

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.getByBranch(ADMIN_USER_ID, 1L, PageRequest.of(0, 10)))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.ACCESS_DENIED));
        verify(staffRepository, never()).findAllByBranchIdAndDeletedAtIsNull(anyLong(), any());
    }

    // --- update ---

    @Test
    void update_success() {
        UpdateStaffRequest request = UpdateStaffRequest.builder()
                .phone("0909999999")
                .branchId(2L)
                .build();

        User user = new User();
        Branch newBranch = branchOwnedBy(ADMIN_USER_ID);
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(branchOwnedBy(ADMIN_USER_ID));

        Staff savedStaff = new Staff();
        StaffResponse expected = StaffResponse.builder().id(1L).build();

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(2L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(newBranch));
        when(staffRepository.save(staff)).thenReturn(savedStaff);
        when(staffMapper.toResponse(savedStaff)).thenReturn(expected);

        StaffResponse result = staffService.update(ADMIN_USER_ID, 1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getPhone()).isEqualTo("0909999999");
        assertThat(staff.getBranch()).isEqualTo(newBranch);
    }

    @Test
    void update_staffNotFound_throwsException() {
        when(staffRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.update(ADMIN_USER_ID, 99L, UpdateStaffRequest.builder().build()))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.STAFF_NOT_FOUND));
    }

    @Test
    void update_branchNotFound_throwsException() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().branchId(99L).build();
        User user = new User();
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(branchOwnedBy(ADMIN_USER_ID));

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(99L, BranchStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.update(ADMIN_USER_ID, 1L, request))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.BRANCH_NOT_FOUND));
    }

    @Test
    void update_onlyPhone_success() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().phone("0911111111").build();
        User user = new User();
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(branchOwnedBy(ADMIN_USER_ID));
        StaffResponse expected = StaffResponse.builder().id(1L).build();

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(staffRepository.save(staff)).thenReturn(staff);
        when(staffMapper.toResponse(staff)).thenReturn(expected);

        StaffResponse result = staffService.update(ADMIN_USER_ID, 1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getPhone()).isEqualTo("0911111111");
    }

    @Test
    void update_onlyBranchId_success() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().branchId(3L).build();
        User user = new User();
        Branch newBranch = branchOwnedBy(ADMIN_USER_ID);
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(branchOwnedBy(ADMIN_USER_ID));
        StaffResponse expected = StaffResponse.builder().id(1L).build();

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(3L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(newBranch));
        when(staffRepository.save(staff)).thenReturn(staff);
        when(staffMapper.toResponse(staff)).thenReturn(expected);

        StaffResponse result = staffService.update(ADMIN_USER_ID, 1L, request);

        assertThat(result).isEqualTo(expected);
        assertThat(staff.getBranch()).isEqualTo(newBranch);
    }

    @Test
    void update_withFullName_updatesFullName() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().fullName("Tran Thi B").build();
        User user = new User();
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(branchOwnedBy(ADMIN_USER_ID));

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(staffRepository.save(staff)).thenReturn(staff);
        when(staffMapper.toResponse(staff)).thenReturn(StaffResponse.builder().id(1L).build());

        staffService.update(ADMIN_USER_ID, 1L, request);

        assertThat(user.getFullName()).isEqualTo("Tran Thi B");
    }

    @Test
    void update_withPassword_encodesAndSetsPassword() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().password("newpass123").build();
        User user = new User();
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(branchOwnedBy(ADMIN_USER_ID));

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(passwordEncoder.encode("newpass123")).thenReturn("encoded_new");
        when(staffRepository.save(staff)).thenReturn(staff);
        when(staffMapper.toResponse(staff)).thenReturn(StaffResponse.builder().id(1L).build());

        staffService.update(ADMIN_USER_ID, 1L, request);

        assertThat(user.getPasswordHash()).isEqualTo("encoded_new");
        verify(passwordEncoder).encode("newpass123");
    }

    @Test
    void update_currentBranchNotOwned_throwsAccessDenied() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().phone("0911111111").build();
        Staff staff = new Staff();
        staff.setUser(new User());
        staff.setBranch(branchOwnedBy(OTHER_ADMIN_ID));

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.update(ADMIN_USER_ID, 1L, request))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.ACCESS_DENIED));
        verify(staffRepository, never()).save(any(Staff.class));
    }

    @Test
    void update_reassignToBranchNotOwned_throwsAccessDenied() {
        UpdateStaffRequest request = UpdateStaffRequest.builder().branchId(2L).build();
        Staff staff = new Staff();
        staff.setUser(new User());
        staff.setBranch(branchOwnedBy(ADMIN_USER_ID));

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(2L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branchOwnedBy(OTHER_ADMIN_ID)));

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.update(ADMIN_USER_ID, 1L, request))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.ACCESS_DENIED));
        verify(staffRepository, never()).save(any(Staff.class));
    }

    // --- delete ---

    @Test
    void delete_success() {
        User user = new User();
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setBranch(branchOwnedBy(ADMIN_USER_ID));

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));

        staffService.delete(ADMIN_USER_ID, 1L);

        assertThat(staff.getDeletedAt()).isNotNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getDeletedAt()).isNotNull();
        verify(userRepository).save(user);
        verify(staffRepository).save(staff);
    }

    @Test
    void delete_staffNotFound_throwsException() {
        when(staffRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.delete(ADMIN_USER_ID, 99L))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.STAFF_NOT_FOUND));
    }

    @Test
    void delete_branchNotOwned_throwsAccessDenied() {
        Staff staff = new Staff();
        staff.setUser(new User());
        staff.setBranch(branchOwnedBy(OTHER_ADMIN_ID));

        when(staffRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(staff));

        assertThatExceptionOfType(AppException.class)
                .isThrownBy(() -> staffService.delete(ADMIN_USER_ID, 1L))
                .satisfies(ex -> assertThat(ex.getErrorEnum()).isEqualTo(ErrorEnum.ACCESS_DENIED));
        verify(staffRepository, never()).save(any(Staff.class));
        verify(userRepository, never()).save(any(User.class));
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

    // --- createWalkInBooking ---

    @Test
    void createWalkInBooking_success() {
        WalkInBookingRequest request = WalkInBookingRequest.builder()
                .staffUserId(STAFF_USER_ID)
                .courtId(COURT_ID)
                .guestPhone("0901234567")
                .slotStarts(List.of(LocalTime.of(8, 0)))
                .build();

        TimeSlotTemplate template = new TimeSlotTemplate();
        template.setPrice(new BigDecimal("50000"));

        Payment savedPayment = new Payment();
        savedPayment.setId(5L);

        Booking savedBooking = new Booking();
        savedBooking.setId(200L);
        savedBooking.setCourt(court);
        savedBooking.setStatus(BookingStatus.CHECKED_IN);
        savedBooking.setTotalPrice(new BigDecimal("50000"));
        savedBooking.setCheckinCode("some-uuid");
        savedBooking.setCheckedInAt(OffsetDateTime.now());

        WalkInBookingResponse expected = WalkInBookingResponse.builder().bookingId(200L).build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID)).thenReturn(Optional.of(staff));
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(bookingSlotRepository.existsByCourt_IdAndBookingDateAndSlotStart(anyLong(), any(), any()))
                .thenReturn(false);
        when(timeSlotTemplateRepository.findByCourtIdAndDayOfWeekAndStartTimeAndActiveTrueAndDeletedAtIsNull(
                anyLong(), anyShort(), any())).thenReturn(Optional.of(template));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingSlotRepository.saveAll(anyList())).thenReturn(List.of());
        when(bookingSlotRepository.findByBooking_IdInOrderByBooking_IdAscSlotStartAsc(anyList()))
                .thenReturn(List.of());
        when(staffScheduleMapper.toWalkInResponse(any(), anyList(), any())).thenReturn(expected);

        WalkInBookingResponse result = staffService.createWalkInBooking(request);

        assertThat(result).isEqualTo(expected);
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingRepository).save(any(Booking.class));
        verify(bookingSlotRepository).saveAll(anyList());
    }

    @Test
    void createWalkInBooking_slotTaken_throwsBadRequest() {
        WalkInBookingRequest request = WalkInBookingRequest.builder()
                .staffUserId(STAFF_USER_ID)
                .courtId(COURT_ID)
                .guestPhone("0901234567")
                .slotStarts(List.of(LocalTime.of(8, 0)))
                .build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID)).thenReturn(Optional.of(staff));
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(bookingSlotRepository.existsByCourt_IdAndBookingDateAndSlotStart(anyLong(), any(), any()))
                .thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.createWalkInBooking(request));
        assertThat(ex.getMessage()).startsWith("Slot already booked");
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createWalkInBooking_courtOtherBranch_throwsBadRequest() {
        Branch otherBranch = new Branch();
        otherBranch.setId(OTHER_BRANCH_ID);
        court.setBranch(otherBranch);

        WalkInBookingRequest request = WalkInBookingRequest.builder()
                .staffUserId(STAFF_USER_ID)
                .courtId(COURT_ID)
                .guestPhone("0901234567")
                .slotStarts(List.of(LocalTime.of(8, 0)))
                .build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID)).thenReturn(Optional.of(staff));
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.createWalkInBooking(request));
        assertEquals("Court belongs to another branch", ex.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createWalkInBooking_noPriceTemplate_throwsBadRequest() {
        WalkInBookingRequest request = WalkInBookingRequest.builder()
                .staffUserId(STAFF_USER_ID)
                .courtId(COURT_ID)
                .guestPhone("0901234567")
                .slotStarts(List.of(LocalTime.of(8, 0)))
                .build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID)).thenReturn(Optional.of(staff));
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(bookingSlotRepository.existsByCourt_IdAndBookingDateAndSlotStart(anyLong(), any(), any()))
                .thenReturn(false);
        when(timeSlotTemplateRepository.findByCourtIdAndDayOfWeekAndStartTimeAndActiveTrueAndDeletedAtIsNull(
                anyLong(), anyShort(), any())).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.createWalkInBooking(request));
        assertThat(ex.getMessage()).startsWith("No price configured for slot");
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createWalkInBooking_duplicateSlotStarts_throwsBadRequest() {
        WalkInBookingRequest request = WalkInBookingRequest.builder()
                .courtId(COURT_ID)
                .guestPhone("0901234567")
                .slotStarts(List.of(LocalTime.of(8, 0), LocalTime.of(8, 0)))
                .build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID))
                .thenReturn(Optional.of(court));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.createWalkInBooking(STAFF_USER_ID, request));
        assertThat(ex.getMessage()).isEqualTo("Duplicate slot starts in request");
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createWalkInBooking_nonConsecutiveSlots_throwsBadRequest() {
        WalkInBookingRequest request = WalkInBookingRequest.builder()
                .courtId(COURT_ID)
                .guestPhone("0901234567")
                .slotStarts(List.of(LocalTime.of(8, 0), LocalTime.of(9, 0)))
                .build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID)).thenReturn(Optional.of(staff));
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(bookingSlotRepository.existsByCourt_IdAndBookingDateAndSlotStart(anyLong(), any(), any()))
                .thenReturn(false);
        when(timeSlotTemplateRepository.findByCourtIdAndDayOfWeekAndStartTimeAndActiveTrueAndDeletedAtIsNull(
                anyLong(), anyShort(), eq(LocalTime.of(8, 0))))
                .thenReturn(Optional.of(template(LocalTime.of(8, 0), LocalTime.of(8, 30), "50000")));
        when(timeSlotTemplateRepository.findByCourtIdAndDayOfWeekAndStartTimeAndActiveTrueAndDeletedAtIsNull(
                anyLong(), anyShort(), eq(LocalTime.of(9, 0))))
                .thenReturn(Optional.of(template(LocalTime.of(9, 0), LocalTime.of(9, 30), "50000")));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.createWalkInBooking(STAFF_USER_ID, request));
        assertEquals("Slots must be consecutive", ex.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createWalkInBooking_multipleConsecutiveSlots_success() {
        WalkInBookingRequest request = WalkInBookingRequest.builder()
                .courtId(COURT_ID)
                .guestPhone("0901234567")
                .slotStarts(List.of(LocalTime.of(8, 0), LocalTime.of(8, 30)))
                .build();

        Payment savedPayment = new Payment();
        savedPayment.setId(5L);

        Booking savedBooking = new Booking();
        savedBooking.setId(200L);
        savedBooking.setCourt(court);

        WalkInBookingResponse expected = WalkInBookingResponse.builder().bookingId(200L).build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID)).thenReturn(Optional.of(staff));
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(bookingSlotRepository.existsByCourt_IdAndBookingDateAndSlotStart(anyLong(), any(), any()))
                .thenReturn(false);
        when(timeSlotTemplateRepository.findByCourtIdAndDayOfWeekAndStartTimeAndActiveTrueAndDeletedAtIsNull(
                anyLong(), anyShort(), eq(LocalTime.of(8, 0))))
                .thenReturn(Optional.of(template(LocalTime.of(8, 0), LocalTime.of(8, 30), "50000")));
        when(timeSlotTemplateRepository.findByCourtIdAndDayOfWeekAndStartTimeAndActiveTrueAndDeletedAtIsNull(
                anyLong(), anyShort(), eq(LocalTime.of(8, 30))))
                .thenReturn(Optional.of(template(LocalTime.of(8, 30), LocalTime.of(9, 0), "60000")));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingSlotRepository.saveAll(anyList())).thenReturn(List.of());
        when(bookingSlotRepository.findByBooking_IdInOrderByBooking_IdAscSlotStartAsc(anyList()))
                .thenReturn(List.of());
        when(staffScheduleMapper.toWalkInResponse(any(), anyList(), any())).thenReturn(expected);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        WalkInBookingResponse result = staffService.createWalkInBooking(STAFF_USER_ID, request);

        assertThat(result).isEqualTo(expected);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getAmount()).isEqualByComparingTo("110000");
        verify(bookingSlotRepository).saveAll(anyList());
    }

    // --- checkout ---

    @Test
    void checkout_success() {
        booking.setStatus(BookingStatus.CHECKED_IN);

        StaffCheckoutResponse expected = StaffCheckoutResponse.builder()
                .bookingId(BOOKING_ID).status(BookingStatus.COMPLETED).build();

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID)).thenReturn(Optional.of(staff));
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(staffScheduleMapper.toCheckoutResponse(booking)).thenReturn(expected);

        StaffCheckoutResponse result = staffService.checkout(STAFF_USER_ID, BOOKING_ID);

        assertThat(result).isEqualTo(expected);
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        assertNotNull(booking.getCompletedAt());
        verify(bookingRepository).save(booking);
    }

    @Test
    void checkout_notCheckedIn_throwsBadRequest() {
        // booking default status is CONFIRMED (set in @BeforeEach)
        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID)).thenReturn(Optional.of(staff));
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.checkout(STAFF_USER_ID, BOOKING_ID));
        assertEquals("Booking has not been checked in yet", ex.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void checkout_otherBranch_throwsBadRequest() {
        booking.setStatus(BookingStatus.CHECKED_IN);
        Branch otherBranch = new Branch();
        otherBranch.setId(OTHER_BRANCH_ID);
        court.setBranch(otherBranch);

        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID)).thenReturn(Optional.of(staff));
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> staffService.checkout(STAFF_USER_ID, BOOKING_ID));
        assertEquals("Booking belongs to another branch", ex.getMessage());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void checkout_bookingNotFound_throws404() {
        when(staffRepository.findByUser_IdAndDeletedAtIsNull(STAFF_USER_ID))
                .thenReturn(Optional.of(staff));
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> staffService.checkout(STAFF_USER_ID, BOOKING_ID));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
