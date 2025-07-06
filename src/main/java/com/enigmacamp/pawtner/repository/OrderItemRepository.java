package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.OrderItem;
import com.enigmacamp.pawtner.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrder(Order order);
}