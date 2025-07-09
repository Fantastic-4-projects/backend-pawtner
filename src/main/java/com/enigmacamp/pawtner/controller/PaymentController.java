package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.service.OrderService;
import com.enigmacamp.pawtner.service.BookingService;
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
    private final BookingService bookingService;

    @PostMapping("/webhook")
    public ResponseEntity<CommonResponse<String>> handleWebhook(@RequestBody Map<String, Object> payload) {
        String orderId = (String) payload.get("order_id");

        if (orderId != null && orderId.startsWith("ORD-")) {
            orderService.handleWebhook(payload);
        } else if (orderId != null && orderId.startsWith("BOOK-")) {
            bookingService.handleWebhook(payload);
        } else {
            // Log or handle unknown orderId format
            System.err.println("Unknown orderId format in webhook: " + orderId);
            return ResponseUtil.createResponse(HttpStatus.BAD_REQUEST, "Unknown orderId format", null);
        }
        return ResponseUtil.createResponse(HttpStatus.OK, "Webhook received", null);
    }
}
