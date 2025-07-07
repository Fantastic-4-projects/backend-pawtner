package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.response.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface OrderService {
    OrderResponseDTO createOrderFromCart(String customerEmail);
    OrderResponseDTO getOrderById(UUID id);
    Page<OrderResponseDTO> getAllOrdersByCustomerId(String customerEmail, Pageable pageable);
    void handleWebhook(Map<String, Object> payload);
    OrderResponseDTO updateOrderStatus(UUID orderId, String newStatus, String businessOwnerEmail);
    Page<OrderResponseDTO> getAllOrdersByBusinessId(UUID businessId, Pageable pageable);
}
