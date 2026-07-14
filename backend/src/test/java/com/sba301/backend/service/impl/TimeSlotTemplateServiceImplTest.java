package com.sba301.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.ApplyTimeSlotTemplateRequest;
import com.sba301.backend.dto.request.CreateTimeSlotTemplateRequest;
import com.sba301.backend.dto.request.UpdateTimeSlotTemplateRequest;
import com.sba301.backend.dto.response.ApplyTimeSlotTemplateResponse;
import com.sba301.backend.dto.response.TimeSlotTemplateResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.Court;
import com.sba301.backend.entity.TimeSlotTemplate;
import com.sba301.backend.entity.User;
import com.sba301.backend.exception.BadRequestException;
import com.sba301.backend.exception.ResourceNotFoundException;
import com.sba301.backend.mapper.TimeSlotTemplateMapper;
import com.sba301.backend.repository.CourtRepository;
import com.sba301.backend.repository.TimeSlotTemplateRepository;
import com.sba301.backend.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class TimeSlotTemplateServiceImplTest {

    private static final Long COURT_ID = 1L;
    private static final Long TEMPLATE_ID = 10L;

    @Mock
    private TimeSlotTemplateRepository timeSlotTemplateRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private CurrentUserService currentUserService;

    private TimeSlotTemplateServiceImpl service;
    private Court court;
    private User admin;

    @BeforeEach
    void setUp() {
        service = new TimeSlotTemplateServiceImpl(
                timeSlotTemplateRepository, courtRepository, new TimeSlotTemplateMapper(), currentUserService);
        admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.SUPER_ADMIN);
        org.mockito.Mockito.lenient().when(currentUserService.getCurrentUser()).thenReturn(admin);
        org.mockito.Mockito.lenient().when(currentUserService.isSuperAdmin(admin)).thenReturn(true);
        court = court(COURT_ID, "Court A");
    }

    @Test
    void createDefaultsActiveToTrue() {
        CreateTimeSlotTemplateRequest request = validCreateRequest();
        request.setActive(null);
        mockExistingCourt(court);
        mockSavedTemplate();

        TimeSlotTemplateResponse response = service.create(request);

        assertEquals(TEMPLATE_ID, response.getId());
        assertEquals(COURT_ID, response.getCourtId());
        assertEquals("Court A", response.getCourtName());
        assertTrue(response.getActive());
    }

    @Test
    void createKeepsExplicitInactiveValue() {
        CreateTimeSlotTemplateRequest request = validCreateRequest();
        request.setActive(false);
        mockExistingCourt(court);
        mockSavedTemplate();

        TimeSlotTemplateResponse response = service.create(request);

        assertFalse(response.getActive());
    }

    @Test
    void createRejectsDuplicateTemplate() {
        CreateTimeSlotTemplateRequest request = validCreateRequest();
        mockExistingCourt(court);
        when(timeSlotTemplateRepository
                .existsByCourtIdAndDayOfWeekAndStartTimeAndEndTimeAndDeletedAtIsNull(
                        COURT_ID, request.getDayOfWeek(), request.getStartTime(), request.getEndTime()))
                .thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class, () -> service.create(request));

        assertEquals("Time slot template already exists for this court", exception.getMessage());
        verify(timeSlotTemplateRepository, never()).save(any(TimeSlotTemplate.class));
    }

    @ParameterizedTest
    @MethodSource("invalidTemplateValues")
    void createRejectsInvalidBusinessValues(
            Short dayOfWeek, LocalTime startTime, LocalTime endTime, BigDecimal price, String message) {
        CreateTimeSlotTemplateRequest request = validCreateRequest();
        request.setDayOfWeek(dayOfWeek);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setPrice(price);
        mockExistingCourt(court);

        BadRequestException exception = assertThrows(
                BadRequestException.class, () -> service.create(request));

        assertEquals(message, exception.getMessage());
        verify(timeSlotTemplateRepository, never()).save(any(TimeSlotTemplate.class));
    }

    @Test
    void createRejectsMissingCourt() {
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> service.create(validCreateRequest()));

        assertEquals("Court not found with id: 1", exception.getMessage());
    }

    @Test
    void getByIdReturnsMappedTemplate() {
        TimeSlotTemplate template = template(court, (short) 1, 6, 7);
        template.setId(TEMPLATE_ID);
        when(timeSlotTemplateRepository.findByIdAndDeletedAtIsNull(TEMPLATE_ID))
                .thenReturn(Optional.of(template));

        TimeSlotTemplateResponse response = service.getById(TEMPLATE_ID);

        assertEquals(TEMPLATE_ID, response.getId());
        assertEquals(COURT_ID, response.getCourtId());
    }

    @Test
    void getByIdRejectsMissingTemplate() {
        when(timeSlotTemplateRepository.findByIdAndDeletedAtIsNull(TEMPLATE_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> service.getById(TEMPLATE_ID));

        assertEquals("Time slot template not found with id: 10", exception.getMessage());
    }

    @Test
    void getAllMapsRepositoryPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        TimeSlotTemplate template = template(court, (short) 1, 6, 7);
        template.setId(TEMPLATE_ID);
        when(timeSlotTemplateRepository.findAllByDeletedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(template), pageable, 1));

        Page<TimeSlotTemplateResponse> result = service.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(TEMPLATE_ID, result.getContent().getFirst().getId());
    }

    @Test
    void getAllAsAdminReturnsOnlyTemplatesInOwnedBranches() {
        admin.setRole(UserRole.ADMIN);
        when(currentUserService.isSuperAdmin(admin)).thenReturn(false);
        PageRequest pageable = PageRequest.of(0, 10);
        TimeSlotTemplate template = template(court, (short) 1, 6, 7);
        template.setId(TEMPLATE_ID);
        when(timeSlotTemplateRepository.findAllByCourtBranchAdminIdAndDeletedAtIsNull(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(template), pageable, 1));

        Page<TimeSlotTemplateResponse> result = service.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(timeSlotTemplateRepository).findAllByCourtBranchAdminIdAndDeletedAtIsNull(1L, pageable);
        verify(timeSlotTemplateRepository, never()).findAllByDeletedAtIsNull(pageable);
    }

    @Test
    void getByIdAsAdminRejectsTemplateInOtherAdminBranch() {
        admin.setRole(UserRole.ADMIN);
        User otherAdmin = new User();
        otherAdmin.setId(2L);
        court.getBranch().setAdmin(otherAdmin);
        TimeSlotTemplate template = existingTemplate();
        when(currentUserService.isSuperAdmin(admin)).thenReturn(false);
        when(timeSlotTemplateRepository.findByIdAndDeletedAtIsNull(TEMPLATE_ID))
                .thenReturn(Optional.of(template));

        AppException exception = assertThrows(AppException.class, () -> service.getById(TEMPLATE_ID));

        assertEquals(ErrorEnum.ACCESS_DENIED, exception.getErrorEnum());
    }

    @Test
    void getByCourtValidatesCourtAndMapsTemplates() {
        TimeSlotTemplate first = template(court, (short) 1, 6, 7);
        TimeSlotTemplate second = template(court, (short) 2, 7, 8);
        mockExistingCourt(court);
        when(timeSlotTemplateRepository.findByCourtIdAndDeletedAtIsNull(COURT_ID))
                .thenReturn(List.of(first, second));

        List<TimeSlotTemplateResponse> result = service.getByCourt(COURT_ID);

        assertEquals(2, result.size());
        assertEquals((short) 2, result.get(1).getDayOfWeek());
    }

    @Test
    void getByCourtRejectsMissingCourt() {
        when(courtRepository.findByIdAndDeletedAtIsNull(COURT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getByCourt(COURT_ID));
        verify(timeSlotTemplateRepository, never()).findByCourtIdAndDeletedAtIsNull(COURT_ID);
    }

    @Test
    void updateChangesEveryEditableField() {
        TimeSlotTemplate template = existingTemplate();
        UpdateTimeSlotTemplateRequest request = UpdateTimeSlotTemplateRequest.builder()
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 30))
                .price(BigDecimal.valueOf(200_000))
                .dayOfWeek((short) 6)
                .active(false)
                .build();
        mockExistingTemplate(template);
        when(timeSlotTemplateRepository.save(template)).thenReturn(template);

        TimeSlotTemplateResponse response = service.update(TEMPLATE_ID, request);

        assertEquals(LocalTime.of(8, 0), response.getStartTime());
        assertEquals(LocalTime.of(9, 30), response.getEndTime());
        assertEquals(BigDecimal.valueOf(200_000), response.getPrice());
        assertEquals((short) 6, response.getDayOfWeek());
        assertFalse(response.getActive());
    }

    @Test
    void updateKeepsExistingValuesWhenRequestFieldsAreNull() {
        TimeSlotTemplate template = existingTemplate();
        UpdateTimeSlotTemplateRequest request = new UpdateTimeSlotTemplateRequest();
        mockExistingTemplate(template);
        when(timeSlotTemplateRepository.save(template)).thenReturn(template);

        TimeSlotTemplateResponse response = service.update(TEMPLATE_ID, request);

        assertEquals(LocalTime.of(6, 0), response.getStartTime());
        assertEquals(LocalTime.of(7, 0), response.getEndTime());
        assertEquals(BigDecimal.valueOf(100_000), response.getPrice());
        assertEquals((short) 1, response.getDayOfWeek());
        assertTrue(response.getActive());
    }

    @Test
    void updateRejectsDuplicateTemplate() {
        TimeSlotTemplate template = existingTemplate();
        UpdateTimeSlotTemplateRequest request = UpdateTimeSlotTemplateRequest.builder()
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 0))
                .build();
        mockExistingTemplate(template);
        when(timeSlotTemplateRepository
                .existsByCourtIdAndDayOfWeekAndStartTimeAndEndTimeAndDeletedAtIsNullAndIdNot(
                        COURT_ID, (short) 1, LocalTime.of(8, 0), LocalTime.of(9, 0), TEMPLATE_ID))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.update(TEMPLATE_ID, request));
        verify(timeSlotTemplateRepository, never()).save(any(TimeSlotTemplate.class));
    }

    @Test
    void updateRejectsMissingTemplate() {
        when(timeSlotTemplateRepository.findByIdAndDeletedAtIsNull(TEMPLATE_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(TEMPLATE_ID, new UpdateTimeSlotTemplateRequest()));
    }

    @Test
    void deleteDeactivatesTemplateAndSetsDeletedAtInsteadOfDeletingEntity() {
        TimeSlotTemplate template = existingTemplate();
        mockExistingTemplate(template);

        service.delete(TEMPLATE_ID);

        assertFalse(template.isActive());
        assertNotNull(template.getDeletedAt());
        verify(timeSlotTemplateRepository).save(template);
        verify(timeSlotTemplateRepository, never()).delete(any(TimeSlotTemplate.class));
    }

    @Test
    void deleteRejectsMissingTemplate() {
        when(timeSlotTemplateRepository.findByIdAndDeletedAtIsNull(TEMPLATE_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(TEMPLATE_ID));
        verify(timeSlotTemplateRepository, never()).save(any(TimeSlotTemplate.class));
    }

    @Test
    void applyCopiesOnlyTemplatesMissingFromTargetCourt() {
        Court sourceCourt = court(1L, "Court A");
        Court targetCourt = court(2L, "Court B");
        TimeSlotTemplate existingTarget = template(targetCourt, (short) 1, 6, 7);
        TimeSlotTemplate existingSource = template(sourceCourt, (short) 1, 6, 7);
        TimeSlotTemplate missingSource = template(sourceCourt, (short) 1, 7, 8);
        missingSource.setPrice(BigDecimal.valueOf(150_000));
        missingSource.setActive(false);
        mockApplyCourts(sourceCourt, targetCourt);
        when(timeSlotTemplateRepository.findByCourtIdAndDeletedAtIsNull(2L))
                .thenReturn(List.of(existingTarget));
        when(timeSlotTemplateRepository.findByCourtIdAndDeletedAtIsNull(1L))
                .thenReturn(List.of(existingSource, missingSource, missingSource));

        ApplyTimeSlotTemplateResponse response = service.applyToCourt(
                2L, ApplyTimeSlotTemplateRequest.builder().sourceCourtId(1L).build());

        assertEquals(1, response.getCopiedCount());
        assertEquals(1L, response.getSourceCourtId());
        assertEquals(2L, response.getTargetCourtId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TimeSlotTemplate>> captor = ArgumentCaptor.forClass(List.class);
        verify(timeSlotTemplateRepository).saveAll(captor.capture());
        TimeSlotTemplate copied = captor.getValue().getFirst();
        assertEquals(targetCourt, copied.getCourt());
        assertEquals(LocalTime.of(7, 0), copied.getStartTime());
        assertEquals(LocalTime.of(8, 0), copied.getEndTime());
        assertEquals((short) 1, copied.getDayOfWeek());
        assertEquals(BigDecimal.valueOf(150_000), copied.getPrice());
        assertFalse(copied.isActive());
    }

    @Test
    void applySavesEmptyListWhenEveryTemplateAlreadyExists() {
        Court sourceCourt = court(1L, "Court A");
        Court targetCourt = court(2L, "Court B");
        TimeSlotTemplate source = template(sourceCourt, (short) 1, 6, 7);
        TimeSlotTemplate target = template(targetCourt, (short) 1, 6, 7);
        mockApplyCourts(sourceCourt, targetCourt);
        when(timeSlotTemplateRepository.findByCourtIdAndDeletedAtIsNull(2L))
                .thenReturn(List.of(target));
        when(timeSlotTemplateRepository.findByCourtIdAndDeletedAtIsNull(1L))
                .thenReturn(List.of(source));

        ApplyTimeSlotTemplateResponse response = service.applyToCourt(
                2L, ApplyTimeSlotTemplateRequest.builder().sourceCourtId(1L).build());

        assertEquals(0, response.getCopiedCount());
        verify(timeSlotTemplateRepository).saveAll(List.of());
    }

    @Test
    void applyRejectsMissingSourceCourt() {
        when(courtRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.applyToCourt(
                2L, ApplyTimeSlotTemplateRequest.builder().sourceCourtId(1L).build()));
        verify(courtRepository, never()).findByIdAndDeletedAtIsNull(2L);
    }

    @Test
    void applyRejectsMissingTargetCourt() {
        Court sourceCourt = court(1L, "Court A");
        when(courtRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(sourceCourt));
        when(courtRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.applyToCourt(
                2L, ApplyTimeSlotTemplateRequest.builder().sourceCourtId(1L).build()));
        verify(timeSlotTemplateRepository, never()).saveAll(any());
    }

    private static Stream<Arguments> invalidTemplateValues() {
        LocalTime start = LocalTime.of(6, 0);
        LocalTime end = LocalTime.of(7, 0);
        BigDecimal validPrice = BigDecimal.ONE;
        return Stream.of(
                Arguments.of(null, start, end, validPrice, "Day of week must be between 0 and 6"),
                Arguments.of((short) -1, start, end, validPrice, "Day of week must be between 0 and 6"),
                Arguments.of((short) 7, start, end, validPrice, "Day of week must be between 0 and 6"),
                Arguments.of((short) 1, null, end, validPrice, "Start time must be before end time"),
                Arguments.of((short) 1, start, null, validPrice, "Start time must be before end time"),
                Arguments.of((short) 1, start, start, validPrice, "Start time must be before end time"),
                Arguments.of((short) 1, end, start, validPrice, "Start time must be before end time"),
                Arguments.of((short) 1, start, end, null, "Price must be greater than 0"),
                Arguments.of((short) 1, start, end, BigDecimal.ZERO, "Price must be greater than 0"),
                Arguments.of((short) 1, start, end, BigDecimal.valueOf(-1), "Price must be greater than 0"));
    }

    private CreateTimeSlotTemplateRequest validCreateRequest() {
        return CreateTimeSlotTemplateRequest.builder()
                .courtId(COURT_ID)
                .dayOfWeek((short) 1)
                .startTime(LocalTime.of(6, 0))
                .endTime(LocalTime.of(7, 0))
                .price(BigDecimal.valueOf(100_000))
                .active(true)
                .build();
    }

    private TimeSlotTemplate existingTemplate() {
        TimeSlotTemplate template = template(court, (short) 1, 6, 7);
        template.setId(TEMPLATE_ID);
        return template;
    }

    private void mockExistingCourt(Court value) {
        when(courtRepository.findByIdAndDeletedAtIsNull(value.getId())).thenReturn(Optional.of(value));
    }

    private void mockExistingTemplate(TimeSlotTemplate template) {
        when(timeSlotTemplateRepository.findByIdAndDeletedAtIsNull(template.getId()))
                .thenReturn(Optional.of(template));
    }

    private void mockSavedTemplate() {
        when(timeSlotTemplateRepository.save(any(TimeSlotTemplate.class)))
                .thenAnswer(invocation -> {
                    TimeSlotTemplate template = invocation.getArgument(0);
                    template.setId(TEMPLATE_ID);
                    return template;
                });
    }

    private void mockApplyCourts(Court sourceCourt, Court targetCourt) {
        when(courtRepository.findByIdAndDeletedAtIsNull(sourceCourt.getId()))
                .thenReturn(Optional.of(sourceCourt));
        when(courtRepository.findByIdAndDeletedAtIsNull(targetCourt.getId()))
                .thenReturn(Optional.of(targetCourt));
    }

    private Court court(Long id, String name) {
        Court value = new Court();
        value.setId(id);
        value.setName(name);
        Branch branch = new Branch();
        branch.setId(id);
        branch.setAdmin(admin);
        value.setBranch(branch);
        return value;
    }

    private TimeSlotTemplate template(Court value, Short dayOfWeek, int startHour, int endHour) {
        TimeSlotTemplate template = new TimeSlotTemplate();
        template.setCourt(value);
        template.setDayOfWeek(dayOfWeek);
        template.setStartTime(LocalTime.of(startHour, 0));
        template.setEndTime(LocalTime.of(endHour, 0));
        template.setPrice(BigDecimal.valueOf(100_000));
        template.setActive(true);
        return template;
    }
}
