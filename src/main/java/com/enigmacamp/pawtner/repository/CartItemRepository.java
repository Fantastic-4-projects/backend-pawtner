package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.CartItem;
import com.enigmacamp.pawtner.entity.Product;
import com.enigmacamp.pawtner.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByShoppingCartAndProduct(ShoppingCart shoppingCart, Product product);
}
