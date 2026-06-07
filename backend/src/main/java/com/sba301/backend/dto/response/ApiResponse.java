package com.sba301.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sba301.backend.common.enums.ErrorEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Integer httpStatus;
    private String errorCode;
    private T data;
    private String message;

    public static <T> ApiResponse<T> success(T data, String message, Integer httpStatus) {
        return ApiResponse.<T>builder()
                .httpStatus(httpStatus)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return success(data, message, 200);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful.", 200);
    }

    public static <T> ApiResponse<T> success() {
        return success(null, "Operation successful.", 200);
    }

    public static <T> ApiResponse<T> failure(Integer httpStatus, String errorCode, String message) {
        return ApiResponse.<T>builder()
                .httpStatus(httpStatus)
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> failure(ErrorEnum errorEnum) {
        return failure(errorEnum.getHttpStatus(), errorEnum.getCode(), errorEnum.getMessage());
    }

    public static ApiResponse<Void> success(String message) {
        return success(null, message, 200);
    }
}

