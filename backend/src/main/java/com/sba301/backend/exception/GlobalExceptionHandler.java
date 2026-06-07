package com.sba301.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi 404 - Không tìm thấy dữ liệu (Nghiệp vụ)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 2. Xử lý lỗi 400 - Dữ liệu không hợp lệ
    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequestException(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 3. Xử lý lỗi 404 - Gọi sai URL API
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Đường dẫn API không tồn tại: " + ex.getRequestURL());
    }

    // 4. Xử lý TẤT CẢ các lỗi hệ thống 500 (Tránh sập server)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        ex.printStackTrace(); // In ra console để dev fix bug
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống đang gặp sự cố, vui lòng thử lại sau!");
    }

    // --- Hàm Helper format JSON (Đồng nhất với ResponseUtil của bạn) ---
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", status.value());
        response.put("message", message);
        response.put("data", null); // Đã lỗi thì không có data
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(status).body(response);
    }
}