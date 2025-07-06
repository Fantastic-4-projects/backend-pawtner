package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.ShoppingCart;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {
    Optional<ShoppingCart> findByCustomerAndBusiness(User customer, Business business);
}
