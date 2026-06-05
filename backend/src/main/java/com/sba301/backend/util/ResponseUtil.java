package com.sba301.backend.util;

import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    // Khai báo static để gọi ở bất kỳ đâu mà không cần khởi tạo (new)
    public static ResponseEntity<Map<String, Object>> buildResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("message", message);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}