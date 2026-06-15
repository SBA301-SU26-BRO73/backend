package com.sba301.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sba301.backend.dto.request.CreatePaymentRequest;
import com.sba301.backend.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse create(CreatePaymentRequest request);

    PaymentResponse getById(Long id);

    Page<PaymentResponse> getAll(Long branchId, Pageable pageable);
}
