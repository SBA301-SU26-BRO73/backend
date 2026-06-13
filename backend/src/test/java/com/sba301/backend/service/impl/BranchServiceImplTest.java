package com.sba301.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.CreateBranchRequest;
import com.sba301.backend.dto.request.UpdateBranchRequest;
import com.sba301.backend.dto.response.BranchResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.User;
import com.sba301.backend.mapper.BranchMapper;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.UserRepository;

import java.time.LocalTime;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BranchMapper branchMapper;

    @InjectMocks
    private BranchServiceImpl branchService;

    private CreateBranchRequest createRequest;
    private UpdateBranchRequest updateRequest;
    private Branch branch;
    private BranchResponse branchResponse;
    private User admin;

    @BeforeEach
    void setUp() {
        // Setup CreateBranchRequest
        createRequest = new CreateBranchRequest();
        createRequest.setAdminId(1L);
        createRequest.setName("Branch 1");
        createRequest.setAddress("123 Main St");
        createRequest.setWard("Ward 1");
        createRequest.setCity("Da Nang");
        createRequest.setPhone("0936849597");
        createRequest.setOpenTime(LocalTime.parse("06:00:00"));
        createRequest.setCloseTime(LocalTime.parse("22:00:00"));
        createRequest.setBankAccountNumber("1234567890");
        createRequest.setBankAccountName("Nguyen Huu Minh Tuan");
        createRequest.setBankName("MB Bank");
        createRequest.setBankQrImageUrl("https://example.com/qr.jpg");

        // Setup UpdateBranchRequest
        updateRequest = new UpdateBranchRequest();
        updateRequest.setAdminId(1L);
        updateRequest.setName("Branch 1 Updated");
        updateRequest.setAddress("124 Main St");
        updateRequest.setWard("Ward 2");
        updateRequest.setCity("Ho Chi Minh");
        updateRequest.setPhone("0987654321");
        updateRequest.setOpenTime(LocalTime.parse("07:00:00"));
        updateRequest.setCloseTime(LocalTime.parse("23:00:00"));
        updateRequest.setBankAccountNumber("0987654321");
        updateRequest.setBankAccountName("Updated Name");
        updateRequest.setBankName("Techcombank");
        updateRequest.setBankQrImageUrl("https://example.com/qr2.jpg");
        updateRequest.setStatus(BranchStatus.ACTIVE);

        // Setup Branch entity
        branch = new Branch();
        branch.setId(1L);
        branch.setName("Branch 1");
        branch.setStatus(BranchStatus.ACTIVE);

        // Setup Admin User
        admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@example.com");

        // Setup BranchResponse
        branchResponse = new BranchResponse();
        branchResponse.setId(1L);
        branchResponse.setName("Branch 1");
        branchResponse.setAdminId(1L);
        branchResponse.setAdminName("admin@example.com");
    }

    @Test
    void testCreateBranchSuccessfully() {
        when(branchRepository.existsByNameAndDeletedAtIsNull("Branch 1")).thenReturn(false);
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(admin));
        when(branchMapper.toEntity(createRequest)).thenReturn(branch);
        when(branchRepository.save(any(Branch.class))).thenReturn(branch);
        when(branchMapper.toResponse(branch)).thenReturn(branchResponse);

        BranchResponse result = branchService.create(createRequest);

        assertNotNull(result);
        assertEquals(branchResponse.getId(), result.getId());
        assertEquals(admin, branch.getAdmin());
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void testCreateBranchWithDuplicateName() {
        when(branchRepository.existsByNameAndDeletedAtIsNull("Branch 1")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> branchService.create(createRequest));

        assertEquals(ErrorEnum.BRANCH_NAME_ALREADY_EXISTS, exception.getErrorEnum());
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void testCreateBranchWithInvalidAdmin() {
        when(branchRepository.existsByNameAndDeletedAtIsNull("Branch 1")).thenReturn(false);
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> branchService.create(createRequest));

        assertEquals(ErrorEnum.ADMIN_NOT_FOUND, exception.getErrorEnum());
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void testGetByIdSuccessfully() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(branchMapper.toResponse(branch)).thenReturn(branchResponse);

        BranchResponse result = branchService.getById(1L);

        assertNotNull(result);
        assertEquals(branchResponse.getId(), result.getId());
        verify(branchRepository).findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE);
    }

    @Test
    void testGetByIdNotFound() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> branchService.getById(1L));

        assertEquals(ErrorEnum.BRANCH_NOT_FOUND, exception.getErrorEnum());
    }

    @Test
    void testGetAllBranches() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Branch> branchPage = new PageImpl<>(java.util.List.of(branch));
        Page<BranchResponse> responsePage = new PageImpl<>(java.util.List.of(branchResponse));

        when(branchRepository.findAllByStatusAndDeletedAtIsNull(BranchStatus.ACTIVE, pageable))
                .thenReturn(branchPage);
        when(branchMapper.toResponse(branch)).thenReturn(branchResponse);

        Page<BranchResponse> result = branchService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(branchResponse.getId(), result.getContent().get(0).getId());
        verify(branchRepository).findAllByStatusAndDeletedAtIsNull(BranchStatus.ACTIVE, pageable);
    }

    @Test
    void testUpdateBranchSuccessfully() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(branchRepository.existsByNameAndDeletedAtIsNull("Branch 1 Updated")).thenReturn(false);
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(admin));
        when(branchRepository.save(any(Branch.class))).thenReturn(branch);
        when(branchMapper.toResponse(branch)).thenReturn(branchResponse);

        BranchResponse result = branchService.update(1L, updateRequest);

        assertNotNull(result);
        verify(branchMapper).updateEntity(branch, updateRequest);
        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void testUpdateBranchWithSameNameSkipsValidation() {
        updateRequest.setName("Branch 1"); // Same as current name
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(admin));
        when(branchRepository.save(any(Branch.class))).thenReturn(branch);
        when(branchMapper.toResponse(branch)).thenReturn(branchResponse);

        BranchResponse result = branchService.update(1L, updateRequest);

        assertNotNull(result);
        verify(branchRepository, never()).existsByNameAndDeletedAtIsNull("Branch 1");
    }

    @Test
    void testUpdateBranchWithDuplicateName() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(branchRepository.existsByNameAndDeletedAtIsNull("Branch 1 Updated")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> branchService.update(1L, updateRequest));

        assertEquals(ErrorEnum.BRANCH_NAME_ALREADY_EXISTS, exception.getErrorEnum());
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void testUpdateBranchNotFound() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> branchService.update(1L, updateRequest));

        assertEquals(ErrorEnum.BRANCH_NOT_FOUND, exception.getErrorEnum());
    }

    @Test
    void testUpdateBranchWithInvalidAdmin() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(branchRepository.existsByNameAndDeletedAtIsNull("Branch 1 Updated")).thenReturn(false);
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> branchService.update(1L, updateRequest));

        assertEquals(ErrorEnum.ADMIN_NOT_FOUND, exception.getErrorEnum());
        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void testDeleteBranchSuccessfully() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));

        branchService.delete(1L);

        assertEquals(BranchStatus.INACTIVE, branch.getStatus());
        assertNotNull(branch.getDeletedAt());
        verify(branchRepository).save(branch);
    }

    @Test
    void testDeleteBranchNotFound() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(1L, BranchStatus.ACTIVE))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> branchService.delete(1L));

        assertEquals(ErrorEnum.BRANCH_NOT_FOUND, exception.getErrorEnum());
        verify(branchRepository, never()).save(any(Branch.class));
    }
}
