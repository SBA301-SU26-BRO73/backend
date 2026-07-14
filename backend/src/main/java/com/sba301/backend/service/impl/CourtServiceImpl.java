package com.sba301.backend.service.impl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.response.DailySlotResponse;
import com.sba301.backend.repository.TimeSlotTemplateRepository;
import com.sba301.backend.repository.projection.DailySlotProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sba301.backend.common.enums.BranchStatus;
import com.sba301.backend.common.enums.CourtStatus;
import com.sba301.backend.dto.request.CreateCourtRequest;
import com.sba301.backend.dto.request.UpdateCourtRequest;
import com.sba301.backend.dto.response.CourtResponse;
import com.sba301.backend.entity.Branch;
import com.sba301.backend.entity.Court;
import com.sba301.backend.entity.CourtType;
import com.sba301.backend.exception.BadRequestException;
import com.sba301.backend.exception.ResourceNotFoundException;
import com.sba301.backend.mapper.CourtMapper;
import com.sba301.backend.repository.BranchRepository;
import com.sba301.backend.repository.CourtRepository;
import com.sba301.backend.repository.CourtTypeRepository;
import com.sba301.backend.entity.User;
import com.sba301.backend.service.CourtPricingService;
import com.sba301.backend.service.CourtService;
import com.sba301.backend.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final CourtTypeRepository courtTypeRepository;
    private final BranchRepository branchRepository;
    private final CourtMapper courtMapper;
    private final CourtPricingService courtPricingService;
    private final TimeSlotTemplateRepository timeSlotTemplateRepository;
    private final CurrentUserService currentUserService;
    @Override
    @Transactional
    public CourtResponse create(CreateCourtRequest request) {
        Branch branch = getBranch(request.getBranchId());
        CourtType courtType = getCourtType(request.getCourtTypeId());
        String name = request.getName().trim();

        validateUniqueName(branch.getId(), name);

        Court court = courtMapper.toEntity(request);
        court.setBranch(branch);
        court.setCourtType(courtType);
        court.setStatus(request.getStatus() == null ? CourtStatus.ACTIVE : request.getStatus());

        Court savedCourt = courtRepository.save(court);
        courtPricingService.applyDefaultPricing(savedCourt);
        return courtMapper.toResponse(savedCourt);
    }

    @Override
    public CourtResponse getById(Long id) {
        return courtMapper.toResponse(getCourt(id));
    }

    @Override
    public Page<CourtResponse> getAll(Pageable pageable) {
        User currentUser = currentUserService.getCurrentUser();
        Page<Court> courts = currentUserService.isSuperAdmin(currentUser)
                ? courtRepository.findAllByDeletedAtIsNull(pageable)
                : courtRepository.findAllByBranchAdminIdAndDeletedAtIsNull(currentUser.getId(), pageable);

        return courts
                .map(courtMapper::toResponse);
    }

    @Override
    @Transactional
    public CourtResponse update(Long id, UpdateCourtRequest request) {
        Court court = getCourt(id);

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BadRequestException("Court name must not be blank");
            }
            if (!court.getName().equalsIgnoreCase(name)) {
                validateUniqueName(court.getBranch().getId(), name, court.getId());
            }
            court.setName(name);
        }

        if (request.getCourtTypeId() != null
                && !request.getCourtTypeId().equals(court.getCourtType().getId())) {
            court.setCourtType(getCourtType(request.getCourtTypeId()));
        }
        if (request.getDescription() != null) {
            court.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            court.setImageUrl(request.getImageUrl());
        }
        if (request.getStatus() != null) {
            court.setStatus(request.getStatus());
        }

        return courtMapper.toResponse(courtRepository.save(court));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Court court = getCourt(id);
        court.setStatus(CourtStatus.INACTIVE);
        court.setDeletedAt(OffsetDateTime.now());
        courtRepository.save(court);
    }

    private Court getCourt(Long id) {
        Court court = courtRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + id));
        assertBranchAccess(court.getBranch());
        return court;
    }

    private Branch getBranch(Long id) {
        Branch branch = branchRepository.findByIdAndStatusAndDeletedAtIsNull(id, BranchStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        assertBranchAccess(branch);
        return branch;
    }

    private CourtType getCourtType(Long id) {
        return courtTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Court type not found with id: " + id));
    }

    private void validateUniqueName(Long branchId, String name) {
        if (courtRepository.existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNull(branchId, name)) {
            throw new BadRequestException("Court name already exists in this branch");
        }
    }

    private void validateUniqueName(Long branchId, String name, Long courtId) {
        if (courtRepository.existsByBranchIdAndNameIgnoreCaseAndDeletedAtIsNullAndIdNot(
                branchId, name, courtId)) {
            throw new BadRequestException("Court name already exists in this branch");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailySlotResponse> getDailyCourtSchedule(Long courtId, LocalDate date) {
        getCourt(courtId);
        return timeSlotTemplateRepository
                .getDailyCourtSchedule(courtId, date)
                .stream()
                .map(courtMapper::toDailySlotResponse)
                .toList();
    }

    @Override
    public List<CourtResponse> getCourtsByBranch(Long branchId) {
        getBranch(branchId);
        return courtRepository.findAllByBranchIdAndDeletedAtIsNull(branchId)
                .stream()
                .map(courtMapper::toResponse)
                .toList();
    }

    private void assertBranchAccess(Branch branch) {
        User currentUser = currentUserService.getCurrentUser();
        if (!currentUserService.isSuperAdmin(currentUser)
                && !branch.getAdmin().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorEnum.ACCESS_DENIED);
        }
    }
}
