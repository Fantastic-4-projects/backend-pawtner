package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.AddToCartRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateCartItemRequestDTO;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;
import com.enigmacamp.pawtner.entity.*;
import com.enigmacamp.pawtner.mapper.ShoppingCartMapper;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        List<CartItem> updatedCartItems = cartItemRepository.findByShoppingCart(shoppingCart);
        return ShoppingCartMapper.mapToResponse(shoppingCart, updatedCartItems);
    }

    @Override
    public ShoppingCartResponseDTO getShoppingCartByCustomerId(String customerEmail) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        List<ShoppingCart> shoppingCarts = shoppingCartRepository.findByCustomer(customer);

        if (shoppingCarts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found for this customer");
        }

        // Assuming one customer can have multiple shopping carts (one per business)
        // For now, we'll return the first one found or handle multiple carts as needed.
        // If a customer can only have one active cart across all businesses, this logic needs adjustment.
        ShoppingCart shoppingCart = shoppingCarts.get(0);
        List<CartItem> cartItems = cartItemRepository.findByShoppingCart(shoppingCart);

        return ShoppingCartMapper.mapToResponse(shoppingCart, cartItems);
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

        ShoppingCart shoppingCart = cartItem.getShoppingCart();
        List<CartItem> updatedCartItems = cartItemRepository.findByShoppingCart(shoppingCart);
        return ShoppingCartMapper.mapToResponse(shoppingCart, updatedCartItems);
    }

    @Override
    @Transactional
    public void removeCartItem(UUID cartItemId, String customerEmail) {
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
        List<ShoppingCart> shoppingCarts = shoppingCartRepository.findByCustomer(customer);

        if (shoppingCarts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found for this customer");
        }

        ShoppingCart shoppingCart = shoppingCarts.get(0);

        List<CartItem> cartItems = cartItemRepository.findByShoppingCart(shoppingCart);

        cartItemRepository.deleteAll(cartItems);
        shoppingCartRepository.delete(shoppingCart);
    }
}