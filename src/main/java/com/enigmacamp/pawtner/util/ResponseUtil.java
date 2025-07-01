package com.enigmacamp.pawtner.util;

import com.enigmacamp.pawtner.dto.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {
    public static <T> ResponseEntity<CommonResponse<T>> createResponse(HttpStatus status, String message, T data) {
        CommonResponse<T> response = new CommonResponse<>();
        response.setMessage(message);
        response.setData(data);

        return ResponseEntity.status(status.value()).body(response);
    }
}
