package com.sba301.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtTypeRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String nameEn;

    private String description;

    @Size(max = 50)
    private String icon;

    @Size(max = 20)
    private String color;

    private boolean active = true;
}
