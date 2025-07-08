package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.CartItemResponseDTO;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;
import com.enigmacamp.pawtner.entity.CartItem;
import com.enigmacamp.pawtner.entity.ShoppingCart;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ShoppingCartMapper {

    public static ShoppingCartResponseDTO mapToResponse(ShoppingCart shoppingCart, List<CartItem> cartItems) {
        List<CartItemResponseDTO> itemDTOs = cartItems.stream()
                .map(CartItemMapper::mapToResponse)
                .collect(Collectors.toList());

        BigDecimal totalPrice = itemDTOs.stream()
                .map(CartItemResponseDTO::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ShoppingCartResponseDTO.builder()
                .id(shoppingCart.getId())
                .customerId(shoppingCart.getCustomer().getId())
                .businessId(shoppingCart.getBusiness().getId())
                .businessName(shoppingCart.getBusiness().getName())
                .items(itemDTOs)
                .totalPrice(totalPrice)
                .build();
    }
}

