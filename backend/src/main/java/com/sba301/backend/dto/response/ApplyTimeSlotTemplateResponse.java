package com.sba301.backend.dto.response;

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
public class ApplyTimeSlotTemplateResponse {

    private Long targetCourtId;
    private Long sourceCourtId;
    private int copiedCount;
}
