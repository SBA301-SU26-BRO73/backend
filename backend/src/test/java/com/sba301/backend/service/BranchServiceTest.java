package com.sba301.backend.service;

import com.sba301.backend.dto.request.BranchFilterDTO;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository; // Làm giả Database

    @InjectMocks
    private BranchService branchService; // Bơm DB giả vào Service

    private List<Branch> mockBranchList;

    @BeforeEach
    void setUp() {
        // Chuẩn bị dữ liệu mẫu
        Branch branch1 = new Branch();
        branch1.setId(1L);
        branch1.setName("Cơ sở Cầu Giấy");

        Branch branch2 = new Branch();
        branch2.setId(2L);
        branch2.setName("Cơ sở Thanh Xuân");

        mockBranchList = Arrays.asList(branch1, branch2);
    }

    @Test
    void testGetAllActiveBranches_ShouldReturnList() {
        // Giả lập: Khi gọi branchRepository.findAll(bất kỳ Specification nào) -> trả về mockBranchList
        Mockito.when(branchRepository.findAll(any(Specification.class))).thenReturn(mockBranchList);

        // Chạy hàm thực tế
        List<Branch> result = branchService.getAllActiveBranches();

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cơ sở Cầu Giấy", result.get(0).getName());
    }

    @Test
    void testSearchAndFilterBranches_ShouldReturnList() {
        // Giả lập filter
        BranchFilterDTO filterDto = new BranchFilterDTO();
        filterDto.setName("Cơ sở");

        // Giả lập DB
        Mockito.when(branchRepository.findAll(any(Specification.class))).thenReturn(mockBranchList);

        // Chạy hàm thực tế
        List<Branch> result = branchService.searchAndFilterBranches(filterDto);

        // Kiểm tra
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}