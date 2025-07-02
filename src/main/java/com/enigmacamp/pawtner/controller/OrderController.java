package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.OrderResponseDTO;
import com.enigmacamp.pawtner.service.OrderService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<OrderResponseDTO>> checkout(Authentication authentication) {
        OrderResponseDTO responseDTO = orderService.createOrderFromCart(authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.CREATED, "Order created successfully", responseDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<OrderResponseDTO>> getOrderById(@PathVariable UUID id) {
        OrderResponseDTO responseDTO = orderService.getOrderById(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched order", responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<Page<OrderResponseDTO>>> getMyOrders(Authentication authentication, Pageable pageable) {
        Page<OrderResponseDTO> responseDTOPage = orderService.getAllOrdersByCustomerId(authentication.getName(), pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all orders", responseDTOPage);
    }
}
