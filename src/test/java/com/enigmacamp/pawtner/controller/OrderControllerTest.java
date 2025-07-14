package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.constant.OrderStatus;
import com.enigmacamp.pawtner.dto.request.OrderRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.OrderPriceCalculationResponseDTO;
import com.enigmacamp.pawtner.dto.response.OrderResponseDTO;
import com.enigmacamp.pawtner.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to create a mock Authentication object
    private Authentication createMockAuthentication(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                new User(username, "", Collections.singletonList(new SimpleGrantedAuthority(role))),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("POST /api/orders/checkout should create an order and return status 201")
    void checkout_shouldCreateOrder_whenValidDataProvided() throws Exception {
        OrderRequestDTO requestDTO = OrderRequestDTO.builder().build(); // Minimal DTO for test
        OrderResponseDTO mockResponse = OrderResponseDTO.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING_PAYMENT)
                .build();

        Authentication mockAuthentication = createMockAuthentication("customer@example.com", "CUSTOMER");

        when(orderService.createOrderFromCart(eq("customer@example.com"), any(OrderRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/orders/checkout")
                        .principal(mockAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-123"));

        verify(orderService, times(1)).createOrderFromCart(eq("customer@example.com"), any(OrderRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/orders/calculate-price should calculate price and return status 200")
    void calculateOrderPrice_shouldCalculatePrice_whenValidDataProvided() throws Exception {
        OrderRequestDTO requestDTO = OrderRequestDTO.builder().build(); // Minimal DTO for test
        OrderPriceCalculationResponseDTO mockResponse = OrderPriceCalculationResponseDTO.builder()
                .totalAmount(BigDecimal.valueOf(150000))
                .build();

        Authentication mockAuthentication = createMockAuthentication("customer@example.com", "CUSTOMER");

        when(orderService.calculateOrderPrice(eq("customer@example.com"), any(OrderRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/orders/calculate-price")
                        .principal(mockAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Order price calculated successfully"))
                .andExpect(jsonPath("$.data.totalAmount").value(150000));

        verify(orderService, times(1)).calculateOrderPrice(eq("customer@example.com"), any(OrderRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/orders/{order_id} should return order by ID and status 200")
    void getOrderById_shouldReturnOrder_whenIdExists() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDTO mockResponse = OrderResponseDTO.builder()
                .id(orderId)
                .orderNumber("ORD-456")
                .build();

        when(orderService.getOrderById(orderId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/orders/{order_id}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched order"))
                .andExpect(jsonPath("$.data.id").value(orderId.toString()))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-456"));

        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    @DisplayName("GET /api/orders should return all orders for customer and status 200")
    void getMyOrders_shouldReturnAllOrders() throws Exception {
        List<OrderResponseDTO> orderList = Collections.singletonList(
                OrderResponseDTO.builder().id(UUID.randomUUID()).orderNumber("ORD-789").build()
        );
        PageImpl<OrderResponseDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(0, 10), 1);

        Authentication mockAuthentication = createMockAuthentication("customer@example.com", "CUSTOMER");

        when(orderService.getAllOrdersByCustomerId(eq("customer@example.com"), any(Pageable.class))).thenReturn(orderPage);

        mockMvc.perform(get("/api/orders")
                        .principal(mockAuthentication)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched all orders"))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("ORD-789"));

        verify(orderService, times(1)).getAllOrdersByCustomerId(eq("customer@example.com"), any(Pageable.class));
    }

    @Test
    @DisplayName("PUT /api/orders/{order_id}/status should update order status and return status 200")
    void updateOrderStatus_shouldUpdateStatus_whenValidDataProvided() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDTO mockResponse = OrderResponseDTO.builder()
                .id(orderId)
                .orderNumber("ORD-101")
                .status(OrderStatus.COMPLETED)
                .build();

        Authentication mockAuthentication = createMockAuthentication("business@example.com", "BUSINESS_OWNER");

        when(orderService.updateOrderStatus(eq(orderId), eq("COMPLETED"), eq("business@example.com"))).thenReturn(mockResponse);

        mockMvc.perform(put("/api/orders/{order_id}/status", orderId)
                        .principal(mockAuthentication)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Order status updated successfully"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq("COMPLETED"), eq("business@example.com"));
    }

    @Test
    @DisplayName("GET /api/orders/business/{businessId} should return all orders for business and status 200")
    void getAllOrdersForBusiness_shouldReturnAllOrders() throws Exception {
        UUID businessId = UUID.randomUUID();
        List<OrderResponseDTO> orderList = Collections.singletonList(
                OrderResponseDTO.builder().id(UUID.randomUUID()).orderNumber("ORD-BUSINESS").build()
        );
        PageImpl<OrderResponseDTO> orderPage = new PageImpl<>(orderList, PageRequest.of(0, 10), 1);

        Authentication mockAuthentication = createMockAuthentication("business@example.com", "BUSINESS_OWNER");

        when(orderService.getAllOrdersByBusinessId(eq(businessId), any(), any(), any(), any(), any(Pageable.class))).thenReturn(orderPage);

        mockMvc.perform(get("/api/orders/business/{businessId}", businessId)
                        .principal(mockAuthentication)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched all orders for business"))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("ORD-BUSINESS"));

        verify(orderService, times(1)).getAllOrdersByBusinessId(eq(businessId), any(), any(), any(), any(), any(Pageable.class));
    }
}
