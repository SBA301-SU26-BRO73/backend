package com.sba301.backend.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sba301.backend.common.enums.ErrorEnum;
import com.sba301.backend.dto.response.ApiResponse;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorEnum errorEnum = ex.getErrorEnum();
        String finalMessage = ex.getMessage();

        log.warn("Business Exception: code={}, message='{}'", errorEnum.getCode(), finalMessage);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .errorCode(errorEnum.getCode())
                .message(finalMessage)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.valueOf(errorEnum.getHttpStatus()));
    }

    // Add
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        log.warn("Validation failed: {}", errors);

        ErrorEnum errorEnum = ErrorEnum.INVALID_INPUT;
        ApiResponse<Map<String, String>> apiResponse = ApiResponse.<Map<String, String>>builder()
                .errorCode(errorEnum.getCode())
                .message(errorEnum.getMessage())
                .data(errors)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.valueOf(errorEnum.getHttpStatus()));
    }

    // Add
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJsonException(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        ErrorEnum errorEnum = ErrorEnum.INVALID_INPUT;
        ApiResponse<Void> apiResponse = ApiResponse.failure(errorEnum);
        return new ResponseEntity<>(apiResponse, HttpStatus.valueOf(errorEnum.getHttpStatus()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        ErrorEnum errorEnum = ErrorEnum.ACCESS_DENIED;
        ApiResponse<Void> apiResponse = ApiResponse.failure(errorEnum);
        return new ResponseEntity<>(apiResponse, HttpStatus.valueOf(errorEnum.getHttpStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUncaughtException(Exception ex) {
        log.error("An unexpected internal server error occurred", ex);

        ErrorEnum errorEnum = ErrorEnum.INTERNAL_SERVER_ERROR;
        ApiResponse<Void> apiResponse = ApiResponse.failure(errorEnum);
        return new ResponseEntity<>(apiResponse, HttpStatus.valueOf(errorEnum.getHttpStatus()));
    }
}