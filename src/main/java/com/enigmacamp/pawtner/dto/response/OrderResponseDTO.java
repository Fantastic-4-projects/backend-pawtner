package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDTO {
    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private String customerName;
    private UUID businessId;
    private String businessName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
    private String snapToken;
    private String redirectUrl;
}
