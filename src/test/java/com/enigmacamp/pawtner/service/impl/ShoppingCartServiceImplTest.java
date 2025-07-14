package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.AddToCartRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateCartItemRequestDTO;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;
import com.enigmacamp.pawtner.entity.*;
import com.enigmacamp.pawtner.repository.CartItemRepository;
import com.enigmacamp.pawtner.repository.ShoppingCartRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.ProductService;
import com.enigmacamp.pawtner.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserService userService;
    @Mock
    private ProductService productService;
    @Mock
    private BusinessService businessService;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    private User customer;
    private Product product;
    private Business business;
    private ShoppingCart shoppingCart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(UUID.randomUUID()).email("customer@example.com").build();
        business = Business.builder().id(UUID.randomUUID()).name("Pet Store").build();
        product = Product.builder().id(UUID.randomUUID()).name("Dog Toy").stockQuantity(10).business(business).price(BigDecimal.TEN).build();
        shoppingCart = ShoppingCart.builder().id(UUID.randomUUID()).customer(customer).business(business).build();
        cartItem = CartItem.builder().id(UUID.randomUUID()).product(product).quantity(1).shoppingCart(shoppingCart).build();
    }


    @Test
    @DisplayName("addItemToCart should throw exception for insufficient stock")
    void addItemToCart_shouldThrowException_forInsufficientStock() {
        product.setStockQuantity(1);
        AddToCartRequestDTO request = new AddToCartRequestDTO(product.getId(), 2, business.getId());

        when(userService.getUserByEmailForInternal(anyString())).thenReturn(customer);
        when(productService.getProductEntityById(any(UUID.class))).thenReturn(product);
        when(businessService.getBusinessByIdForInternal(any(UUID.class))).thenReturn(business);

        assertThatThrownBy(() -> shoppingCartService.addItemToCart(request, customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not enough stock for product");
    }

    @Test
    @DisplayName("getShoppingCartByCustomerId should throw exception if no cart found")
    void getShoppingCartByCustomerId_shouldThrowException_whenNotFound() {
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(customer);
        when(shoppingCartRepository.findByCustomer(customer)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> shoppingCartService.getShoppingCartByCustomerId(customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Shopping cart not found for this customer");
    }

    @Test
    @DisplayName("removeCartItem should delete the item")
    void removeCartItem_shouldSucceed() {
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(customer);
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));

        shoppingCartService.removeCartItem(cartItem.getId(), customer.getEmail());

        verify(cartItemRepository).delete(cartItem);
    }
}