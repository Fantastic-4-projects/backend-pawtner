package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.OrderItemResponseDTO;
import com.enigmacamp.pawtner.dto.response.OrderResponseDTO;
import com.enigmacamp.pawtner.entity.Order;
import com.enigmacamp.pawtner.entity.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {
    public static OrderResponseDTO mapToResponse(Order order, List<OrderItem> orderItems) {
        List<OrderItemResponseDTO> itemDTOs = orderItems.stream()
                .map(OrderItemMapper::mapToResponse)
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .businessId(order.getBusiness().getId())
                .businessName(order.getBusiness().getName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(itemDTOs)
                .build();
    }
}
