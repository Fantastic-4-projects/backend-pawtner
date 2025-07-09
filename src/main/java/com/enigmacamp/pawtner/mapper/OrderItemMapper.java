package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.OrderItemResponseDTO;
import com.enigmacamp.pawtner.entity.OrderItem;

import java.math.BigDecimal;

public class OrderItemMapper {
    public static OrderItemResponseDTO mapToResponse(OrderItem orderItem) {
        return OrderItemResponseDTO.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct().getId())
                .productName(orderItem.getProduct().getName())
                .quantity(orderItem.getQuantity())
                .pricePerUnit(orderItem.getPricePerUnit())
                .subTotal(orderItem.getPricePerUnit().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .imageUrl(orderItem.getProduct().getImageUrl())
                .build();
    }
}
