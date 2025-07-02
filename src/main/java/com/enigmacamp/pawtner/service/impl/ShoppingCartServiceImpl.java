package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.AddToCartRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateCartItemRequestDTO;
import com.enigmacamp.pawtner.dto.response.CartItemResponseDTO;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;
import com.enigmacamp.pawtner.entity.*;
import com.enigmacamp.pawtner.repository.CartItemRepository;
import com.enigmacamp.pawtner.repository.ShoppingCartRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.ProductService;
import com.enigmacamp.pawtner.service.ShoppingCartService;
import com.enigmacamp.pawtner.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;
    private final BusinessService businessService;

    @Override
    @Transactional
    public ShoppingCartResponseDTO addItemToCart(AddToCartRequestDTO addToCartRequestDTO, String customerEmail) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        Product product = productService.getProductEntityById(addToCartRequestDTO.getProductId());
        Business business = businessService.getBusinessByIdForInternal(addToCartRequestDTO.getBusinessId());

        if (!product.getBusiness().getId().equals(business.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product does not belong to the specified business");
        }

        if (product.getStockQuantity() < addToCartRequestDTO.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + product.getName());
        }

        ShoppingCart shoppingCart = shoppingCartRepository.findByCustomerAndBusiness(customer, business)
                .orElseGet(() -> shoppingCartRepository.save(ShoppingCart.builder()
                        .customer(customer)
                        .business(business)
                        .build()));

        Optional<CartItem> existingCartItem = cartItemRepository.findByShoppingCartAndProduct(shoppingCart, product);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + addToCartRequestDTO.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .shoppingCart(shoppingCart)
                    .product(product)
                    .quantity(addToCartRequestDTO.getQuantity())
                    .build();
        }
        cartItemRepository.save(cartItem);

        return getShoppingCartByCustomerId(customerEmail);
    }

    @Override
    public ShoppingCartResponseDTO getShoppingCartByCustomerId(String customerEmail) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        List<ShoppingCart> shoppingCarts = shoppingCartRepository.findAll().stream()
                .filter(cart -> cart.getCustomer().getId().equals(customer.getId()))
                .collect(Collectors.toList());

        if (shoppingCarts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found for this customer");
        }

        // Assuming one shopping cart per customer for simplicity, or you might need to choose one if multiple exist
        // For now, let's return the first one found or modify to return a list of carts if needed
        ShoppingCart shoppingCart = shoppingCarts.get(0);

        List<CartItem> cartItems = cartItemRepository.findAll().stream()
                .filter(item -> item.getShoppingCart().getId().equals(shoppingCart.getId()))
                .collect(Collectors.toList());

        List<CartItemResponseDTO> itemDTOs = cartItems.stream()
                .map(this::mapToCartItemResponseDTO)
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

    @Override
    @Transactional
    public ShoppingCartResponseDTO updateCartItemQuantity(UpdateCartItemRequestDTO updateCartItemRequestDTO, String customerEmail) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        CartItem cartItem = cartItemRepository.findById(updateCartItemRequestDTO.getCartItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!cartItem.getShoppingCart().getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cart item does not belong to the authenticated user");
        }

        if (cartItem.getProduct().getStockQuantity() < updateCartItemRequestDTO.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + cartItem.getProduct().getName());
        }

        cartItem.setQuantity(updateCartItemRequestDTO.getQuantity());
        cartItemRepository.save(cartItem);

        return getShoppingCartByCustomerId(customerEmail);
    }

    @Override
    @Transactional
    public void removeCartItem(Integer cartItemId, String customerEmail) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        if (!cartItem.getShoppingCart().getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cart item does not belong to the authenticated user");
        }
        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearShoppingCart(String customerEmail) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        List<ShoppingCart> shoppingCarts = shoppingCartRepository.findAll().stream()
                .filter(cart -> cart.getCustomer().getId().equals(customer.getId()))
                .collect(Collectors.toList());

        if (shoppingCarts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found for this customer");
        }

        // Assuming one shopping cart per customer for simplicity
        ShoppingCart shoppingCart = shoppingCarts.get(0);

        List<CartItem> cartItems = cartItemRepository.findAll().stream()
                .filter(item -> item.getShoppingCart().getId().equals(shoppingCart.getId()))
                .collect(Collectors.toList());

        cartItemRepository.deleteAll(cartItems);
        shoppingCartRepository.delete(shoppingCart);
    }

    private CartItemResponseDTO mapToCartItemResponseDTO(CartItem cartItem) {
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
