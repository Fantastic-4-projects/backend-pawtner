package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.OrderStatus;
import com.enigmacamp.pawtner.constant.PaymentStatus;
import com.enigmacamp.pawtner.dto.response.OrderItemResponseDTO;
import com.enigmacamp.pawtner.dto.response.OrderResponseDTO;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;
import com.enigmacamp.pawtner.entity.*;
import com.enigmacamp.pawtner.repository.OrderItemRepository;
import com.enigmacamp.pawtner.repository.OrderRepository;
import com.enigmacamp.pawtner.service.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartService shoppingCartService;
    private final UserService userService;
    private final ProductService productService;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public OrderResponseDTO createOrderFromCart(String customerEmail) {
        ShoppingCartResponseDTO shoppingCartDTO = shoppingCartService.getShoppingCartByCustomerId(customerEmail);

        if (shoppingCartDTO.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");
        }

        User customer = userService.getUserByEmailForInternal(customerEmail);
        Business business = new Business();
        business.setId(shoppingCartDTO.getBusinessId());
        business.setName(shoppingCartDTO.getBusinessName());

        // Deduct stock and create order items
        List<OrderItem> orderItems = shoppingCartDTO.getItems().stream().map(cartItemDTO -> {
            Product product = productService.getProductEntityById(cartItemDTO.getProductId());
            if (product.getStockQuantity() < cartItemDTO.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItemDTO.getQuantity());
            // No need to save product here, as it will be saved by cascade or transaction commit

            return OrderItem.builder()
                    .product(product)
                    .quantity(cartItemDTO.getQuantity())
                    .pricePerUnit(cartItemDTO.getProductPrice())
                    .build();
        }).collect(Collectors.toList());

        // Create order
        Order order = Order.builder()
                .customer(customer)
                .business(business)
                .orderNumber(generateOrderNumber())
                .totalAmount(shoppingCartDTO.getTotalPrice())
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        orderRepository.save(order);

        // Link order items to order and save
        orderItems.forEach(orderItem -> {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        });

        // Create payment
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentService.createPayment(payment);

        // Clear the shopping cart after successful order creation
        shoppingCartService.clearShoppingCart(customerEmail);

        OrderResponseDTO responseDTO = mapToOrderResponseDTO(order, orderItems);
        responseDTO.setSnapToken(payment.getSnapToken());
        return responseDTO;
    }

    @Override
    public OrderResponseDTO getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        return mapToOrderResponseDTO(order, orderItems);
    }

    @Override
    public Page<OrderResponseDTO> getAllOrdersByCustomerId(String customerEmail, Pageable pageable) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        Page<Order> orders = orderRepository.findByCustomer(customer, pageable);
        return orders.map(order -> mapToOrderResponseDTO(order, orderItemRepository.findByOrder(order)));
    }

    @Override
    public void handleWebhook(Map<String, Object> payload) {
        String orderId = (String) payload.get("order_id");
        String transactionStatus = (String) payload.get("transaction_status");
        String fraudStatus = (String) payload.get("fraud_status");

        Order order = orderRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (transactionStatus.equals("capture")) {
            if (fraudStatus.equals("accept")) {
                order.setStatus(OrderStatus.PROCESSING);
            }
        } else if (transactionStatus.equals("settlement")) {
            order.setStatus(OrderStatus.COMPLETED);
        } else if (transactionStatus.equals("cancel") || transactionStatus.equals("deny") || transactionStatus.equals("expire")) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order, List<OrderItem> orderItems) {
        List<OrderItemResponseDTO> itemDTOs = orderItems.stream()
                .map(orderItem -> OrderItemResponseDTO.builder()
                        .id(orderItem.getId())
                        .productId(orderItem.getProduct().getId())
                        .productName(orderItem.getProduct().getName())
                        .quantity(orderItem.getQuantity())
                        .pricePerUnit(orderItem.getPricePerUnit())
                        .subTotal(orderItem.getPricePerUnit().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .businessId(order.getBusiness().getId())
                .businessName(order.getBusiness().getName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(itemDTOs)
                .build();
    }
}