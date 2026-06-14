package com.sba301.backend.common.enums;

import lombok.Getter;

@Getter
public enum ErrorEnum {
        ACCESS_DENIED(403, "access_denied", "You do not have permission to access this resource"),
        INVALID_INPUT(400, "invalid_input", "Invalid input data"),
        INVALID_INPUT_COMMON(400, "invalid_input", "%s"),
        BRANCH_NOT_FOUND(404, "branch_not_found", "Branch not found"),
        ADMIN_NOT_FOUND(404, "admin_not_found", "Admin not found"),
        BRANCH_NAME_ALREADY_EXISTS(400, "branch_name_already_exists", "Branch name already exists"),
        INTERNAL_SERVER_ERROR(500, "internal_server_error", "An error occurred. Please try again later."),
        COURT_TYPE_NOT_FOUND(404, "court_type_not_found", "Court type not found"),
        COURT_TYPE_NAME_ALREADY_EXISTS(409, "court_type_name_already_exists", "Court type name already exists"),
        SUBSCRIPTION_PLAN_NOT_FOUND(404, "subscription_plan_not_found", "Subscription plan not found"),
        SUBSCRIPTION_PLAN_NAME_ALREADY_EXISTS(409, "subscription_plan_name_already_exists", "Subscription plan name already exists");
        
        private final int httpStatus;
        private final String code;
        private final String message;


        ErrorEnum(int httpStatus, String code, String message) {
                this.httpStatus = httpStatus;
                this.code = code;
                this.message = message;
        }
}
