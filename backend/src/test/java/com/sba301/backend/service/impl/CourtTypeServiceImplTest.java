package com.sba301.backend.service.impl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.CourtTypeRequest;
import com.sba301.backend.dto.response.CourtTypeResponse;
import com.sba301.backend.entity.CourtType;
import com.sba301.backend.mapper.CourtTypeMapper;
import com.sba301.backend.repository.CourtTypeRepository;

@ExtendWith(MockitoExtension.class)
class CourtTypeServiceImplTest {

    @Mock private CourtTypeRepository courtTypeRepository;
    @Mock private CourtTypeMapper courtTypeMapper;

    @InjectMocks
    private CourtTypeServiceImpl courtTypeService;

    private CourtType courtType;
    private CourtTypeResponse courtTypeResponse;
    private CourtTypeRequest courtTypeRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        courtType = new CourtType();
        courtType.setId(1L);
        courtType.setName("Cầu lông");
        courtType.setActive(true);

        courtTypeResponse = CourtTypeResponse.builder()
                .id(1L).name("Cầu lông").active(true).build();

        courtTypeRequest = new CourtTypeRequest("Cầu lông", "Badminton", "Môn cầu lông", "shuttle", "#15803D", true);
    }

    @Test
    void getAll_NoFilter_ShouldReturnPage() {
        when(courtTypeRepository.findAllActive(pageable)).thenReturn(new PageImpl<>(List.of(courtType)));
        when(courtTypeMapper.toResponse(any())).thenReturn(courtTypeResponse);

        Page<CourtTypeResponse> result = courtTypeService.getAll(null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(courtTypeRepository).findAllActive(pageable);
    }

    @Test
    void getAll_FilterByActive_ShouldCallFindAllByActive() {
        when(courtTypeRepository.findAllByActive(true, pageable)).thenReturn(new PageImpl<>(List.of(courtType)));
        when(courtTypeMapper.toResponse(any())).thenReturn(courtTypeResponse);

        courtTypeService.getAll(true, pageable);

        verify(courtTypeRepository).findAllByActive(true, pageable);
    }

    @Test
    void getById_Success_ShouldReturnResponse() {
        when(courtTypeRepository.findByIdActive(1L)).thenReturn(Optional.of(courtType));
        when(courtTypeMapper.toResponse(courtType)).thenReturn(courtTypeResponse);

        CourtTypeResponse result = courtTypeService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getById_NotFound_ShouldThrow() {
        when(courtTypeRepository.findByIdActive(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> courtTypeService.getById(99L));
        assertEquals(ErrorEnum.COURT_TYPE_NOT_FOUND, ex.getErrorEnum());
    }

    @Test
    void create_Success_ShouldReturnResponse() {
        when(courtTypeRepository.existsByNameAndNotDeleted("Cầu lông")).thenReturn(false);
        when(courtTypeMapper.toEntity(courtTypeRequest)).thenReturn(courtType);
        when(courtTypeRepository.save(courtType)).thenReturn(courtType);
        when(courtTypeMapper.toResponse(courtType)).thenReturn(courtTypeResponse);

        CourtTypeResponse result = courtTypeService.create(courtTypeRequest);

        assertNotNull(result);
        verify(courtTypeRepository).save(courtType);
    }

    @Test
    void create_NameAlreadyExists_ShouldThrow() {
        when(courtTypeRepository.existsByNameAndNotDeleted("Cầu lông")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> courtTypeService.create(courtTypeRequest));
        assertEquals(ErrorEnum.COURT_TYPE_NAME_ALREADY_EXISTS, ex.getErrorEnum());
        verify(courtTypeRepository, never()).save(any());
    }

    @Test
    void update_Success_ShouldReturnResponse() {
        courtType.setName("Tên cũ");
        when(courtTypeRepository.findByIdActive(1L)).thenReturn(Optional.of(courtType));
        when(courtTypeRepository.existsByNameAndNotDeleted("Cầu lông")).thenReturn(false);
        when(courtTypeRepository.save(courtType)).thenReturn(courtType);
        when(courtTypeMapper.toResponse(courtType)).thenReturn(courtTypeResponse);

        CourtTypeResponse result = courtTypeService.update(1L, courtTypeRequest);

        assertNotNull(result);
        verify(courtTypeMapper).updateFromRequest(courtType, courtTypeRequest);
    }

    @Test
    void update_SameNameNotDuplicate_ShouldNotThrow() {
        courtType.setName("Cầu lông");
        when(courtTypeRepository.findByIdActive(1L)).thenReturn(Optional.of(courtType));
        when(courtTypeRepository.save(courtType)).thenReturn(courtType);
        when(courtTypeMapper.toResponse(courtType)).thenReturn(courtTypeResponse);

        courtTypeService.update(1L, courtTypeRequest);

        verify(courtTypeRepository, never()).existsByNameAndNotDeleted(any());
    }

    @Test
    void update_NotFound_ShouldThrow() {
        when(courtTypeRepository.findByIdActive(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> courtTypeService.update(99L, courtTypeRequest));
        assertEquals(ErrorEnum.COURT_TYPE_NOT_FOUND, ex.getErrorEnum());
    }

    @Test
    void update_NameAlreadyExists_ShouldThrow() {
        CourtType existing = new CourtType();
        existing.setId(1L);
        existing.setName("Cũ");
        when(courtTypeRepository.findByIdActive(1L)).thenReturn(Optional.of(existing));
        when(courtTypeRepository.existsByNameAndNotDeleted("Cầu lông")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> courtTypeService.update(1L, courtTypeRequest));
        assertEquals(ErrorEnum.COURT_TYPE_NAME_ALREADY_EXISTS, ex.getErrorEnum());
    }

    @Test
    void delete_Success_ShouldSetDeletedAt() {
        when(courtTypeRepository.findByIdActive(1L)).thenReturn(Optional.of(courtType));

        courtTypeService.delete(1L);

        assertNotNull(courtType.getDeletedAt());
        verify(courtTypeRepository).save(courtType);
    }

    @Test
    void delete_NotFound_ShouldThrow() {
        when(courtTypeRepository.findByIdActive(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> courtTypeService.delete(99L));
        assertEquals(ErrorEnum.COURT_TYPE_NOT_FOUND, ex.getErrorEnum());
    }
}
