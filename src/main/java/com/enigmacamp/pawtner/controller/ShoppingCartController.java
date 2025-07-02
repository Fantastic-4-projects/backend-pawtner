package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.AddToCartRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateCartItemRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;
import com.enigmacamp.pawtner.service.ShoppingCartService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@AllArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<ShoppingCartResponseDTO>> addItemToCart(@Valid @RequestBody AddToCartRequestDTO addToCartRequestDTO, Authentication authentication) {
        ShoppingCartResponseDTO responseDTO = shoppingCartService.addItemToCart(addToCartRequestDTO, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.CREATED, "Successfully added item to cart", responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<ShoppingCartResponseDTO>> getShoppingCart(Authentication authentication) {
        ShoppingCartResponseDTO responseDTO = shoppingCartService.getShoppingCartByCustomerId(authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched shopping cart", responseDTO);
    }

    @PutMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<ShoppingCartResponseDTO>> updateCartItemQuantity(@Valid @RequestBody UpdateCartItemRequestDTO updateCartItemRequestDTO, Authentication authentication) {
        ShoppingCartResponseDTO responseDTO = shoppingCartService.updateCartItemQuantity(updateCartItemRequestDTO, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully updated cart item quantity", responseDTO);
    }

    @DeleteMapping("/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<Void>> removeCartItem(@PathVariable UUID cartItemId, Authentication authentication) {
        shoppingCartService.removeCartItem(cartItemId, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully removed item from cart", null);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CommonResponse<Void>> clearShoppingCart(Authentication authentication) {
        shoppingCartService.clearShoppingCart(authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully cleared shopping cart", null);
    }
}