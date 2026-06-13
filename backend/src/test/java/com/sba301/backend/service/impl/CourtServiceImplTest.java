package com.sba301.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

import com.sba301.backend.dto.request.CreateCourtRequest;
import com.sba301.backend.dto.request.UpdateCourtRequest;
import com.sba301.backend.dto.response.CourtResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.BranchStatus;
import com.sba301.backend.entity.Court;
import com.sba301.backend.entity.CourtStatus;
import com.sba301.backend.entity.CourtType;
import com.sba301.backend.exception.BadRequestException;
import com.sba301.backend.exception.ResourceNotFoundException;
import com.sba301.backend.mapper.CourtMapper;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.CourtRepository;
import com.sba301.backend.repository.CourtTypeRepository;
import com.sba301.backend.service.CourtPricingService;

@ExtendWith(MockitoExtension.class)
class CourtServiceImplTest {

    private static final Long BRANCH_ID = 1L;
    private static final Long COURT_TYPE_ID = 2L;
    private static final Long COURT_ID = 3L;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private CourtTypeRepository courtTypeRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private CourtMapper courtMapper;

    @Mock
    private CourtPricingService courtPricingService;

    @InjectMocks
    private CourtServiceImpl courtService;

    private CreateCourtRequest createRequest;
    private Branch branch;
    private CourtType courtType;
    private Court court;
    private CourtResponse courtResponse;

    @BeforeEach
    void setUp() {
        createRequest = CreateCourtRequest.builder()
                .branchId(BRANCH_ID)
                .name(" Court 1 ")
                .courtTypeId(COURT_TYPE_ID)
                .description("Indoor court")
                .imageUrl("https://example.com/court.jpg")
                .build();

        branch = new Branch();
        branch.setId(BRANCH_ID);
        branch.setName("Branch 1");

        courtType = new CourtType();
        courtType.setId(COURT_TYPE_ID);
        courtType.setName("Badminton");

        court = new Court();
        court.setId(COURT_ID);
        court.setBranch(branch);
        court.setCourtType(courtType);
        court.setName("Court 1");
        court.setDescription("Indoor court");
        court.setImageUrl("https://example.com/court.jpg");
        court.setStatus(CourtStatus.ACTIVE);

        courtResponse = CourtResponse.builder()
                .id(COURT_ID)
                .branchId(BRANCH_ID)
                .branchName(branch.getName())
                .name(court.getName())
                .courtTypeId(COURT_TYPE_ID)
                .courtTypeName(courtType.getName())
                .status(CourtStatus.ACTIVE)
                .build();
    }

    @Test
    void createDefaultsStatusToActiveAndAppliesDefaultPricing() {
        mockCreateDependencies();

        CourtResponse result = courtService.create(createRequest);

        assertSame(courtResponse, result);
        assertSame(branch, court.getBranch());
        assertSame(courtType, court.getCourtType());
        assertEquals(CourtStatus.ACTIVE, court.getStatus());
        verify(courtRepository).existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNull(
                BRANCH_ID, "Court 1");
        verify(courtRepository).save(court);
        verify(courtPricingService).applyDefaultPricing(court);
    }

    @Test
    void createUsesRequestedStatusWhenProvided() {
        createRequest.setStatus(CourtStatus.MAINTENANCE);
        mockCreateDependencies();

        courtService.create(createRequest);

        assertEquals(CourtStatus.MAINTENANCE, court.getStatus());
    }

    @Test
    void createThrowsWhenBranchDoesNotExist() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(BRANCH_ID, BranchStatus.ACTIVE))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> courtService.create(createRequest));

        assertEquals("Branch not found with id: 1", exception.getMessage());
        verifyNoInteractions(courtTypeRepository, courtMapper, courtPricingService);
        verify(courtRepository, never()).save(any(Court.class));
    }

    @Test
    void createThrowsWhenCourtTypeDoesNotExist() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(BRANCH_ID, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(courtTypeRepository.findById(COURT_TYPE_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> courtService.create(createRequest));

        assertEquals("Court type not found with id: 2", exception.getMessage());
        verifyNoInteractions(courtMapper, courtPricingService);
        verify(courtRepository, never()).save(any(Court.class));
    }

    @Test
    void createRejectsDuplicateNameWithinSameBranch() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(BRANCH_ID, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(courtTypeRepository.findById(COURT_TYPE_ID)).thenReturn(Optional.of(courtType));
        when(courtRepository.existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNull(
                BRANCH_ID, "Court 1")).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> courtService.create(createRequest));

        assertEquals("Court name already exists in this branch", exception.getMessage());
        verify(courtRepository, never()).save(any(Court.class));
        verifyNoInteractions(courtMapper, courtPricingService);
    }

    @Test
    void getByIdReturnsNonDeletedCourt() {
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(courtMapper.toResponse(court)).thenReturn(courtResponse);

        CourtResponse result = courtService.getById(COURT_ID);

        assertSame(courtResponse, result);
        verify(courtRepository).findByIdAndDeletedAtIsNull(COURT_ID);
    }

    @Test
    void getByIdThrowsWhenCourtDoesNotExistOrWasDeleted() {
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> courtService.getById(COURT_ID));

        assertEquals("Court not found with id: 3", exception.getMessage());
        verifyNoInteractions(courtMapper);
    }

    @Test
    void getAllReturnsOnlyRepositoryPageOfNonDeletedCourts() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<Court> courtPage = new PageImpl<>(List.of(court), pageable, 6);
        when(courtRepository.findAllByDeletedAtIsNull(pageable)).thenReturn(courtPage);
        when(courtMapper.toResponse(court)).thenReturn(courtResponse);

        Page<CourtResponse> result = courtService.getAll(pageable);

        assertEquals(6, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getNumber());
        assertSame(courtResponse, result.getContent().getFirst());
        verify(courtRepository).findAllByDeletedAtIsNull(pageable);
    }

    @Test
    void updateChangesAllAllowedFieldsAndLoadsNewCourtType() {
        CourtType newCourtType = new CourtType();
        newCourtType.setId(4L);
        newCourtType.setName("Tennis");
        UpdateCourtRequest request = UpdateCourtRequest.builder()
                .name(" Court 2 ")
                .courtTypeId(4L)
                .description("Outdoor court")
                .imageUrl("https://example.com/new.jpg")
                .status(CourtStatus.MAINTENANCE)
                .build();
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(courtRepository.existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(
                BRANCH_ID, "Court 2", COURT_ID)).thenReturn(false);
        when(courtTypeRepository.findById(4L)).thenReturn(Optional.of(newCourtType));
        when(courtRepository.save(court)).thenReturn(court);
        when(courtMapper.toResponse(court)).thenReturn(courtResponse);

        CourtResponse result = courtService.update(COURT_ID, request);

        assertSame(courtResponse, result);
        assertEquals("Court 2", court.getName());
        assertSame(newCourtType, court.getCourtType());
        assertEquals("Outdoor court", court.getDescription());
        assertEquals("https://example.com/new.jpg", court.getImageUrl());
        assertEquals(CourtStatus.MAINTENANCE, court.getStatus());
        verify(courtRepository).save(court);
    }

    @Test
    void updateWithSameNameAndCourtTypeSkipsDuplicateCheckAndTypeLookup() {
        UpdateCourtRequest request = UpdateCourtRequest.builder()
                .name("court 1")
                .courtTypeId(COURT_TYPE_ID)
                .build();
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(courtRepository.save(court)).thenReturn(court);
        when(courtMapper.toResponse(court)).thenReturn(courtResponse);

        courtService.update(COURT_ID, request);

        assertEquals("court 1", court.getName());
        verify(courtRepository, never())
                .existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(
                        any(), any(), any());
        verifyNoInteractions(courtTypeRepository);
    }

    @Test
    void updateWithNullFieldsKeepsCurrentValues() {
        UpdateCourtRequest request = new UpdateCourtRequest();
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(courtRepository.save(court)).thenReturn(court);
        when(courtMapper.toResponse(court)).thenReturn(courtResponse);

        courtService.update(COURT_ID, request);

        assertEquals("Court 1", court.getName());
        assertSame(courtType, court.getCourtType());
        assertEquals("Indoor court", court.getDescription());
        assertEquals("https://example.com/court.jpg", court.getImageUrl());
        assertEquals(CourtStatus.ACTIVE, court.getStatus());
        verifyNoInteractions(courtTypeRepository);
    }

    @Test
    void updateRejectsBlankName() {
        UpdateCourtRequest request = UpdateCourtRequest.builder().name("   ").build();
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> courtService.update(COURT_ID, request));

        assertEquals("Court name must not be blank", exception.getMessage());
        verify(courtRepository, never()).save(any(Court.class));
    }

    @Test
    void updateRejectsDuplicateNameWithinSameBranch() {
        UpdateCourtRequest request = UpdateCourtRequest.builder().name("Court 2").build();
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(courtRepository.existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(
                BRANCH_ID, "Court 2", COURT_ID)).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> courtService.update(COURT_ID, request));

        assertEquals("Court name already exists in this branch", exception.getMessage());
        verify(courtRepository, never()).save(any(Court.class));
    }

    @Test
    void updateThrowsWhenNewCourtTypeDoesNotExist() {
        UpdateCourtRequest request = UpdateCourtRequest.builder().courtTypeId(4L).build();
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));
        when(courtTypeRepository.findById(4L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> courtService.update(COURT_ID, request));

        assertEquals("Court type not found with id: 4", exception.getMessage());
        verify(courtRepository, never()).save(any(Court.class));
    }

    @Test
    void updateThrowsWhenCourtDoesNotExist() {
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> courtService.update(COURT_ID, new UpdateCourtRequest()));

        verify(courtRepository, never()).save(any(Court.class));
        verifyNoInteractions(courtTypeRepository);
    }

    @Test
    void deleteSoftDeletesCourtAndSetsInactiveStatus() {
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.of(court));

        courtService.delete(COURT_ID);

        assertEquals(CourtStatus.INACTIVE, court.getStatus());
        assertNotNull(court.getDeletedAt());
        verify(courtRepository).save(court);
        verify(courtRepository, never()).delete(any(Court.class));
        verify(courtRepository, never()).deleteById(any());
    }

    @Test
    void deleteThrowsWhenCourtDoesNotExistOrWasAlreadyDeleted() {
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> courtService.delete(COURT_ID));

        verify(courtRepository, never()).save(any(Court.class));
        verify(courtRepository, never()).delete(any(Court.class));
    }

    private void mockCreateDependencies() {
        when(branchRepository.findByIdAndStatusAndDeletedAtIsNull(BRANCH_ID, BranchStatus.ACTIVE))
                .thenReturn(Optional.of(branch));
        when(courtTypeRepository.findById(COURT_TYPE_ID)).thenReturn(Optional.of(courtType));
        when(courtRepository.existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNull(
                BRANCH_ID, "Court 1")).thenReturn(false);
        when(courtMapper.toEntity(createRequest)).thenReturn(court);
        when(courtRepository.save(court)).thenReturn(court);
        when(courtMapper.toResponse(court)).thenReturn(courtResponse);
    }
}
