package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.dto.request.AddToCartRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateCartItemRequestDTO;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;
import com.enigmacamp.pawtner.service.ShoppingCartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShoppingCartController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Authentication createMockAuthentication(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                new User(username, "", Collections.singletonList(new SimpleGrantedAuthority(role))),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("POST /api/cart should add item to cart and return status 201")
    void addItemToCart_shouldSucceed() throws Exception {
        AddToCartRequestDTO request = AddToCartRequestDTO.builder()
                .productId(UUID.randomUUID())
                .quantity(1)
                .businessId(UUID.randomUUID())
                .build();
        ShoppingCartResponseDTO response = ShoppingCartResponseDTO.builder().id(UUID.randomUUID()).build();
        Authentication auth = createMockAuthentication("customer@example.com", "CUSTOMER");

        when(shoppingCartService.addItemToCart(any(AddToCartRequestDTO.class), eq("customer@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/cart")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Successfully added item to cart"));
    }

    @Test
    @DisplayName("GET /api/cart should return shopping cart and status 200")
    void getShoppingCart_shouldSucceed() throws Exception {
        ShoppingCartResponseDTO response = ShoppingCartResponseDTO.builder().id(UUID.randomUUID()).build();
        Authentication auth = createMockAuthentication("customer@example.com", "CUSTOMER");

        when(shoppingCartService.getShoppingCartByCustomerId("customer@example.com")).thenReturn(response);

        mockMvc.perform(get("/api/cart").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(response.getId().toString()));
    }

    @Test
    @DisplayName("PUT /api/cart should update item quantity and return status 200")
    void updateCartItemQuantity_shouldSucceed() throws Exception {
        UpdateCartItemRequestDTO request = UpdateCartItemRequestDTO.builder()
                .cartItemId(UUID.randomUUID())
                .quantity(3)
                .build();
        ShoppingCartResponseDTO response = ShoppingCartResponseDTO.builder().id(UUID.randomUUID()).build();
        Authentication auth = createMockAuthentication("customer@example.com", "CUSTOMER");

        when(shoppingCartService.updateCartItemQuantity(any(UpdateCartItemRequestDTO.class), eq("customer@example.com"))).thenReturn(response);

        mockMvc.perform(put("/api/cart")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully updated cart item quantity"));
    }

    @Test
    @DisplayName("DELETE /api/cart/{cartItemId} should remove item and return status 200")
    void removeCartItem_shouldSucceed() throws Exception {
        UUID cartItemId = UUID.randomUUID();
        Authentication auth = createMockAuthentication("customer@example.com", "CUSTOMER");
        doNothing().when(shoppingCartService).removeCartItem(cartItemId, auth.getName());

        mockMvc.perform(delete("/api/cart/{cartItemId}", cartItemId).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully removed item from cart"));

        verify(shoppingCartService, times(1)).removeCartItem(cartItemId, auth.getName());
    }

    @Test
    @DisplayName("DELETE /api/cart should clear cart and return status 200")
    void clearShoppingCart_shouldSucceed() throws Exception {
        Authentication auth = createMockAuthentication("customer@example.com", "CUSTOMER");
        doNothing().when(shoppingCartService).clearShoppingCart(auth.getName());

        mockMvc.perform(delete("/api/cart").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully cleared shopping cart"));

        verify(shoppingCartService, times(1)).clearShoppingCart(auth.getName());
    }
}