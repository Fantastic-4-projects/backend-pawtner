package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.OrderItem;
import com.enigmacamp.pawtner.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrder(Order order);
}
