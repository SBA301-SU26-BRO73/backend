package com.sba301.backend.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.sba301.backend.common.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long branchId;
    private String branchName;
    private BigDecimal amount;
    private String billImageUrl;
    private PaymentStatus status;
    private OffsetDateTime confirmedAt;
    private Long confirmedById;
    private List<PaymentBookingResponse> bookings;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
