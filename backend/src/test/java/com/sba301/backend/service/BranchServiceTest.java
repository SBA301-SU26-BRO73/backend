package com.sba301.backend.service;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.BranchFilterDTO;
import com.sba301.backend.dto.response.BranchResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.service.impl.BranchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository; // Làm giả Database

    @InjectMocks
    private BranchServiceImpl branchService; // Bơm DB giả vào Class Impl

    private List<Branch> mockBranchList;
    private Branch mockBranch;

    @BeforeEach
    void setUp() {
        // Chuẩn bị dữ liệu mẫu
        mockBranch = new Branch();
        mockBranch.setId(1L);
        mockBranch.setName("Cơ sở Cầu Giấy");
        // THÊM DÒNG NÀY ĐỂ FIX LỖI NULL:
        mockBranch.setCreatedAt(OffsetDateTime.now());

        Branch branch2 = new Branch();
        branch2.setId(2L);
        branch2.setName("Cơ sở Thanh Xuân");
        // THÊM DÒNG NÀY ĐỂ FIX LỖI NULL:
        branch2.setCreatedAt(OffsetDateTime.now());

        mockBranchList = Arrays.asList(mockBranch, branch2);
    }

    // ==========================================
    // 1. TEST API GET BY ID
    // ==========================================
    @Test
    void testGetBranchById_Success() {
        Mockito.when(branchRepository.findById(1L)).thenReturn(Optional.of(mockBranch));

        BranchResponse result = branchService.getBranchById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Cơ sở Cầu Giấy", result.getName());
    }

    @Test
    void testGetBranchById_NotFound_ShouldThrowAppException() {
        Mockito.when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> branchService.getBranchById(99L)
        );

        // Kiểm tra đúng mã lỗi 404 từ ErrorEnum
        assertEquals(ErrorEnum.RESOURCE_NOT_FOUND, exception.getErrorEnum());
        assertEquals("Không tìm thấy cơ sở nào với ID = 99", exception.getCustomMessage());
    }

    // ==========================================
    // 2. TEST API GET ALL (KHÔNG PHÂN TRANG)
    // ==========================================
    @Test
    void testGetAllActiveBranches_ShouldReturnList() {
        Mockito.when(branchRepository.findAll(any(Specification.class))).thenReturn(mockBranchList);

        List<BranchResponse> result = branchService.getAllActiveBranches();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cơ sở Cầu Giấy", result.get(0).getName());
    }

    // ==========================================
    // 3. TEST API GET ALL (CÓ PHÂN TRANG)
    // ==========================================
    @Test
    void testGetAllActiveBranchesPaginated_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Branch> mockPage = new PageImpl<>(mockBranchList, pageable, mockBranchList.size());

        Mockito.when(branchRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        Page<BranchResponse> result = branchService.getAllActiveBranchesPaginated(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals("Cơ sở Thanh Xuân", result.getContent().get(1).getName());
    }

    // ==========================================
    // 4. TEST API TÌM KIẾM/LỌC (CÓ PHÂN TRANG)
    // ==========================================
    @Test
    void testSearchBranchesWithPagination_ShouldReturnPage() {
        BranchFilterDTO filterDto = new BranchFilterDTO();
        filterDto.setName("Cơ sở");

        Pageable pageable = PageRequest.of(0, 5);
        Page<Branch> mockPage = new PageImpl<>(mockBranchList, pageable, mockBranchList.size());

        Mockito.when(branchRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(mockPage);

        Page<BranchResponse> result = branchService.searchBranchesWithPagination(filterDto, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }
}