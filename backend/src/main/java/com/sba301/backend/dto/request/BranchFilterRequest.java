package com.sba301.backend.dto.request;

import lombok.Data;

@Data
public class BranchFilterRequest {
    private String name;
    private String address;
    private String ward;
    private String city;
    private String phone;
    private String status;
    private String courtTypeName;
}