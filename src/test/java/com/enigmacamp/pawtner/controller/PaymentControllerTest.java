package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.service.BookingService;
import com.enigmacamp.pawtner.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/payments/webhook should handle order webhook and return status 200")
    void handleWebhook_shouldHandleOrderWebhook_whenOrderIdStartsWithORD() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", "ORD-12345");
        payload.put("transaction_status", "settlement");

        doNothing().when(orderService).handleWebhook(anyMap());

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Webhook received"));

        verify(orderService, times(1)).handleWebhook(anyMap());
        verify(bookingService, never()).handleWebhook(anyMap());
    }

    @Test
    @DisplayName("POST /api/payments/webhook should handle booking webhook and return status 200")
    void handleWebhook_shouldHandleBookingWebhook_whenOrderIdStartsWithBOOK() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", "BOOK-67890");
        payload.put("transaction_status", "settlement");

        doNothing().when(bookingService).handleWebhook(anyMap());

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Webhook received"));

        verify(bookingService, times(1)).handleWebhook(anyMap());
        verify(orderService, never()).handleWebhook(anyMap());
    }

    @Test
    @DisplayName("POST /api/payments/webhook should return 400 for unknown orderId format")
    void handleWebhook_shouldReturnBadRequest_whenOrderIdHasUnknownFormat() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", "UNKNOWN-123");
        payload.put("transaction_status", "settlement");

        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Unknown orderId format"));

        verify(orderService, never()).handleWebhook(anyMap());
        verify(bookingService, never()).handleWebhook(anyMap());
    }
}
