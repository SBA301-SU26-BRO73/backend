package com.sba301.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sba301.backend.dto.request.CourtTypeRequest;
import com.sba301.backend.dto.response.ApiResponse;
import com.sba301.backend.dto.response.CourtTypeResponse;
import com.sba301.backend.service.CourtTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/court-types")
@RequiredArgsConstructor
public class CourtTypeController {

    private final CourtTypeService courtTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourtTypeResponse>>> getAll(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(courtTypeService.getAll(active, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourtTypeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courtTypeService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourtTypeResponse>> create(@Valid @RequestBody CourtTypeRequest request) {
        CourtTypeResponse response = courtTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Court type created.", 201));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourtTypeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CourtTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courtTypeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        courtTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Court type deleted."));
    }
}
