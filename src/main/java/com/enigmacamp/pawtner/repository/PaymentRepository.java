package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Order;
import com.enigmacamp.pawtner.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByPaymentGatewayRefId(String refId);
}
