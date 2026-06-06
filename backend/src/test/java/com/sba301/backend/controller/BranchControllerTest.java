package com.sba301.backend.controller;

import com.sba301.backend.dto.request.BranchFilterDTO;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.service.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BranchController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
public class BranchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BranchService branchService;

    private List<Branch> mockList;

    @BeforeEach
    void setUp() {
        Branch b1 = new Branch();
        b1.setId(1L);
        b1.setName("Cơ sở 1");

        Branch b2 = new Branch();
        b2.setId(2L);
        b2.setName("Cơ sở 2");

        mockList = Arrays.asList(b1, b2);
    }

    @Test
    void testGetAllBranches_ShouldReturnSuccessMessage() throws Exception {
        Mockito.when(branchService.getAllActiveBranches()).thenReturn(mockList);

        mockMvc.perform(get("/api/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách cơ sở thành công"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testSearchBranches_WithData_ShouldReturnSuccessMessage() throws Exception {
        Mockito.when(branchService.searchAndFilterBranches(any(BranchFilterDTO.class))).thenReturn(mockList);

        mockMvc.perform(get("/api/branches/search")
                        .param("name", "Cơ sở")
                        .param("city", "Hà Nội"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Tìm kiếm và lọc cơ sở thành công"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testSearchBranches_NoData_ShouldReturnEmptyMessage() throws Exception {
        Mockito.when(branchService.searchAndFilterBranches(any(BranchFilterDTO.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/branches/search")
                        .param("name", "Tên ảo không tồn tại"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Không tìm thấy cơ sở nào phù hợp"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}