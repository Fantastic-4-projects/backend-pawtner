package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.CartItemResponseDTO;
import com.enigmacamp.pawtner.entity.CartItem;

import java.math.BigDecimal;

public class CartItemMapper {
    public static CartItemResponseDTO mapToResponse(CartItem cartItem) {
        BigDecimal subTotal = cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        return CartItemResponseDTO.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .productPrice(cartItem.getProduct().getPrice())
                .quantity(cartItem.getQuantity())
                .subTotal(subTotal)
                .build();
    }
}
