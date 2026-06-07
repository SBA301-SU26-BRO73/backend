package com.sba301.backend.common.enums;

import lombok.Getter;

@Getter
public enum ErrorEnum {
        ACCESS_DENIED(403, "access_denied", "You do not have permission to access this resource"),
        INVALID_INPUT(400, "invalid_input", "Invalid input data"),
        INVALID_INPUT_COMMON(400, "invalid_input", "%s"),
        INTERNAL_SERVER_ERROR(500, "internal_server_error", "An error occurred. Please try again later.");
        
        private final int httpStatus;
        private final String code;
        private final String message;


        ErrorEnum(int httpStatus, String code, String message) {
                this.httpStatus = httpStatus;
                this.code = code;
                this.message = message;
        }
}