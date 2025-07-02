package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.AddToCartRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateCartItemRequestDTO;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;

import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartResponseDTO addItemToCart(AddToCartRequestDTO addToCartRequestDTO, String customerEmail);
    ShoppingCartResponseDTO getShoppingCartByCustomerId(String customerEmail);
    ShoppingCartResponseDTO updateCartItemQuantity(UpdateCartItemRequestDTO updateCartItemRequestDTO, String customerEmail);
    void removeCartItem(Integer cartItemId, String customerEmail);
    void clearShoppingCart(String customerEmail);
}
