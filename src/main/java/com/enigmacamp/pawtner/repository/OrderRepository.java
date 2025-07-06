package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Order;
import com.enigmacamp.pawtner.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByCustomer(User customer, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByBusiness(Business business, Pageable pageable);
}