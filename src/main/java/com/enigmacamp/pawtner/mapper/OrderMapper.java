package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.OrderItemResponseDTO;
import com.enigmacamp.pawtner.dto.response.OrderResponseDTO;
import com.enigmacamp.pawtner.entity.Order;
import com.enigmacamp.pawtner.entity.OrderItem;
import com.enigmacamp.pawtner.repository.PaymentRepository;
import com.enigmacamp.pawtner.entity.Payment;
import java.math.BigDecimal;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {
    public static OrderResponseDTO mapToResponse(Order order, List<OrderItem> orderItems, PaymentRepository paymentRepository, BigDecimal shippingFee) {
        List<OrderItemResponseDTO> itemDTOs = orderItems.stream()
                .map(OrderItemMapper::mapToResponse)
                .collect(Collectors.toList());

        String snapToken = null;
        String redirectUrl = null;

        // Attempt to find the associated Payment for the Order
        Payment payment = paymentRepository.findByPaymentGatewayRefId(order.getOrderNumber()).orElse(null);
        if (payment != null) {
            snapToken = payment.getSnapToken();
            redirectUrl = payment.getRedirectUrl();
        }

        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customer(UserMapper.mapToResponse(order.getCustomer()))
                .businessId(order.getBusiness().getId())
                .businessName(order.getBusiness().getName())
                .totalAmount(order.getTotalAmount())
                .shippingFee(shippingFee)
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(itemDTOs)
                .snapToken(snapToken)
                .redirectUrl(redirectUrl)
                .build();
    }
}
