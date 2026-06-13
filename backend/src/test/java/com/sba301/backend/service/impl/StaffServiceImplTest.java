package com.sba301.backend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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

import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.CreateStaffRequest;
import com.sba301.backend.dto.request.UpdateStaffRequest;
import com.sba301.backend.dto.response.StaffResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.Staff;
import com.sba301.backend.entity.User;
import com.sba301.backend.mapper.StaffMapper;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.StaffRepository;
import com.sba301.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

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

    @InjectMocks
    private StaffServiceImpl staffService;

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
}
