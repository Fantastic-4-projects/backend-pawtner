package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.service.OrderService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@AllArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    @PostMapping("/webhook")
    public ResponseEntity<CommonResponse<String>> handleWebhook(@RequestBody Map<String, Object> payload) {
        orderService.handleWebhook(payload);
        return ResponseUtil.createResponse(HttpStatus.OK, "Webhook received", null);
    }
}
