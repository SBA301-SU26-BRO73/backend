package com.sba301.backend.service.impl;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.config.exception.AppException;
import com.sba301.backend.dto.request.CourtTypeRequest;
import com.sba301.backend.dto.response.CourtTypeResponse;
import com.sba301.backend.entity.CourtType;
import com.sba301.backend.mapper.CourtTypeMapper;
import com.sba301.backend.repository.CourtTypeRepository;
import com.sba301.backend.service.CourtTypeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourtTypeServiceImpl implements CourtTypeService {

    private final CourtTypeRepository courtTypeRepository;
    private final CourtTypeMapper courtTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CourtTypeResponse> getAll(Boolean active, Pageable pageable) {
        Page<CourtType> page = (active != null)
                ? courtTypeRepository.findAllByActive(active, pageable)
                : courtTypeRepository.findAllActive(pageable);
        return page.map(courtTypeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourtTypeResponse getById(Long id) {
        return courtTypeMapper.toResponse(getActive(id));
    }

    @Override
    @Transactional
    public CourtTypeResponse create(CourtTypeRequest request) {
        if (courtTypeRepository.existsByNameAndNotDeleted(request.getName())) {
            throw new AppException(ErrorEnum.COURT_TYPE_NAME_ALREADY_EXISTS);
        }
        return courtTypeMapper.toResponse(courtTypeRepository.save(courtTypeMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public CourtTypeResponse update(Long id, CourtTypeRequest request) {
        CourtType ct = getActive(id);
        if (!ct.getName().equals(request.getName()) && courtTypeRepository.existsByNameAndNotDeleted(request.getName())) {
            throw new AppException(ErrorEnum.COURT_TYPE_NAME_ALREADY_EXISTS);
        }
        courtTypeMapper.updateFromRequest(ct, request);
        return courtTypeMapper.toResponse(courtTypeRepository.save(ct));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CourtType ct = getActive(id);
        ct.setDeletedAt(OffsetDateTime.now());
        courtTypeRepository.save(ct);
    }

    private CourtType getActive(Long id) {
        return courtTypeRepository.findByIdActive(id)
                .orElseThrow(() -> new AppException(ErrorEnum.COURT_TYPE_NOT_FOUND));
    }
}
