package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.constant.OrderStatus;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.OrderPriceCalculationResponseDTO;
import com.enigmacamp.pawtner.dto.response.OrderResponseDTO;
import com.enigmacamp.pawtner.service.OrderService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<OrderResponseDTO>> checkout(
            Authentication authentication,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        OrderResponseDTO responseDTO = orderService.createOrderFromCart(authentication.getName(), latitude, longitude);
        return ResponseUtil.createResponse(HttpStatus.CREATED, "Order created successfully", responseDTO);
    }

    @GetMapping("/calculate-price")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<OrderPriceCalculationResponseDTO>> calculateOrderPrice(
            Authentication authentication,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        OrderPriceCalculationResponseDTO responseDTO = orderService.calculateOrderPrice(authentication.getName(), latitude, longitude);
        return ResponseUtil.createResponse(HttpStatus.OK, "Order price calculated successfully", responseDTO);
    }

    @GetMapping("/{order_id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('BUSINESS_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<OrderResponseDTO>> getOrderById(@PathVariable(name = "order_id") UUID id) {
        OrderResponseDTO responseDTO = orderService.getOrderById(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched order", responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<Page<OrderResponseDTO>>> getMyOrders(Authentication authentication, Pageable pageable) {
        Page<OrderResponseDTO> responseDTOPage = orderService.getAllOrdersByCustomerId(authentication.getName(), pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all orders", responseDTOPage);
    }

    @PutMapping("/{order_id}/status")
    @PreAuthorize("hasAuthority('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<OrderResponseDTO>> updateOrderStatus(@PathVariable(name = "order_id") UUID id, @RequestParam String status, Authentication authentication) {
        OrderResponseDTO responseDTO = orderService.updateOrderStatus(id, status, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Order status updated successfully", responseDTO);
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasAuthority('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<Page<OrderResponseDTO>>> getAllOrdersForBusiness(
            @PathVariable UUID businessId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String nameCustomer,
            @RequestParam(required = false) String emailCustomer,
            @RequestParam(required = false) OrderStatus orderStatus
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<OrderResponseDTO> responseDTOPage = orderService.getAllOrdersByBusinessId(businessId, orderNumber, nameCustomer, emailCustomer, orderStatus,pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all orders for business", responseDTOPage);
    }
}
