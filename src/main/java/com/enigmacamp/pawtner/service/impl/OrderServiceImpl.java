package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.OrderStatus;
import com.enigmacamp.pawtner.constant.PaymentStatus;
import com.enigmacamp.pawtner.dto.response.OrderPriceCalculationResponseDTO;
import com.enigmacamp.pawtner.dto.response.OrderResponseDTO;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;
import com.enigmacamp.pawtner.entity.*;
import com.enigmacamp.pawtner.mapper.OrderMapper;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.repository.OrderItemRepository;
import com.enigmacamp.pawtner.repository.OrderRepository;
import com.enigmacamp.pawtner.repository.PaymentRepository;
import com.enigmacamp.pawtner.service.*;


import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartService shoppingCartService;
    private final UserService userService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;
    private final BusinessService businessService;
    private final BusinessRepository businessRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();


    @Override
    @Transactional
    public OrderResponseDTO createOrderFromCart(String customerEmail, Double latitude, Double longitude) {
        ShoppingCartResponseDTO shoppingCartDTO = shoppingCartService.getShoppingCartByCustomerId(customerEmail);

        if (shoppingCartDTO.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");
        }

        User customer = userService.getUserByEmailForInternal(customerEmail);
        Business business = businessService.getBusinessByIdForInternal(shoppingCartDTO.getBusinessId());

        // Calculate shipping fee
        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        userLocation.setSRID(4326); // Set SRID to 4326
        Double distanceInMeters = businessRepository.calculateDistanceToBusiness(business.getId(), userLocation);
        double shippingFee = calculateShippingFee(distanceInMeters);
        BigDecimal roundedShippingFee = BigDecimal.valueOf(shippingFee).setScale(2, RoundingMode.HALF_UP);

        // Calculate total amount including shipping fee
        BigDecimal totalAmountWithShipping = shoppingCartDTO.getTotalPrice().add(roundedShippingFee, new MathContext(18, RoundingMode.HALF_UP));

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
                .totalAmount(totalAmountWithShipping)
                .shippingFee(roundedShippingFee)
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

        // Send notification to customer
        notificationService.sendNotification(
                customer,
                "Order Successful!",
                "Your order " + order.getOrderNumber() + " has been placed.",
                Collections.singletonMap("orderId", order.getId().toString())
        );


        OrderResponseDTO responseDTO = OrderMapper.mapToResponse(order, orderItems, paymentRepository, roundedShippingFee);
        responseDTO.setSnapToken(payment.getSnapToken());
    // Gunakan redirect_url dari Midtrans, bukan format manual
    responseDTO.setRedirectUrl(payment.getRedirectUrl()); // Asumsi Payment entity memiliki field redirectUrl
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        return OrderMapper.mapToResponse(order, orderItems, paymentRepository, order.getShippingFee());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrdersByCustomerId(String customerEmail, Pageable pageable) {
        User customer = userService.getUserByEmailForInternal(customerEmail);
        Page<Order> orders = orderRepository.findByCustomer(customer, pageable);
        return orders.map(order ->  OrderMapper.mapToResponse(order, orderItemRepository.findByOrder(order), paymentRepository, order.getShippingFee()));
    }

    @Override @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        String orderId = (String) payload.get("order_id");
        String transactionStatus = (String) payload.get("transaction_status");
        String fraudStatus = (String) payload.get("fraud_status");

        System.out.println("Webhook received: " + payload);

        Order order = orderRepository.findByOrderNumber(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = getOrderStatus(oldStatus, transactionStatus, fraudStatus);

        if (oldStatus != newStatus) {
            order.setStatus(newStatus);
            orderRepository.save(order);

            // Update payment status
            Payment payment = paymentRepository.findByOrder(order)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
            payment.setStatus(PaymentStatus.valueOf(transactionStatus.toUpperCase()));
            paymentRepository.save(payment);

            // Send notification to customer
            notificationService.sendNotification(
                    order.getCustomer(),
                    "Order Status Updated",
                    "Your order " + order.getOrderNumber() + " is now " + newStatus.name(),
                    Collections.singletonMap("orderId", order.getId().toString())
            );
        }
    }

    private static OrderStatus getOrderStatus(OrderStatus oldStatus, String transactionStatus, String fraudStatus) {
        OrderStatus newStatus = oldStatus;

        if (transactionStatus.equals("capture")) {
            if (fraudStatus.equals("accept")) {
                newStatus = OrderStatus.PROCESSING;
            }
        } else if (transactionStatus.equals("settlement")) {
            newStatus = OrderStatus.COMPLETED;
        } else if (transactionStatus.equals("cancel") || transactionStatus.equals("deny") || transactionStatus.equals("expire")) {
            newStatus = OrderStatus.CANCELLED;
        } else if (transactionStatus.equals("pending")) {
            newStatus = OrderStatus.PENDING_PAYMENT;
        }
        return newStatus;
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(UUID orderId, String newStatus, String businessOwnerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        Business business = businessService.getBusinessByOwnerEmailForInternal(businessOwnerEmail);

        if (!order.getBusiness().getId().equals(business.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to this business owner");
        }

        try {
            OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
            order.setStatus(status);
            orderRepository.save(order);

            // Send notification to customer about order status update
            notificationService.sendNotification(
                    order.getCustomer(),
                    "Order Status Updated",
                    "Your order " + order.getOrderNumber() + " is now " + status.name(),
                    Collections.singletonMap("orderId", order.getId().toString())
            );

            return OrderMapper.mapToResponse(order, orderItemRepository.findByOrder(order), paymentRepository, order.getShippingFee());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status: " + newStatus);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrdersByBusinessId(UUID businessId, Pageable pageable) {
        Business business = businessService.getBusinessByIdForInternal(businessId);
        Page<Order> orders = orderRepository.findByBusiness(business, pageable);
        return orders.map(order -> OrderMapper.mapToResponse(order, orderItemRepository.findByOrder(order), paymentRepository, order.getShippingFee()));
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public double calculateShippingFee(double distanceInMeters) {
        // Example: Base fee + cost per kilometer
        double baseFee = 5000; // Rp 5.000
        double costPerKm = 1000; // Rp 1.000 per kilometer

        // Convert meters to kilometers
        double distanceInKm = distanceInMeters / 1000;

        return baseFee + (costPerKm * distanceInKm);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPriceCalculationResponseDTO calculateOrderPrice(String customerEmail, Double latitude, Double longitude) {
        ShoppingCartResponseDTO shoppingCartDTO = shoppingCartService.getShoppingCartByCustomerId(customerEmail);

        if (shoppingCartDTO.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");
        }

        // Subtotal is the total price from the shopping cart before shipping fee
        BigDecimal subtotal = shoppingCartDTO.getTotalPrice();

        // Get business associated with the shopping cart items
        // Assuming all items in the cart belong to the same business
        Business business = businessService.getBusinessByIdForInternal(shoppingCartDTO.getBusinessId());

        // Calculate shipping fee
        Point userLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        userLocation.setSRID(4326); // Set SRID to 4326
        Double distanceInMeters = businessRepository.calculateDistanceToBusiness(business.getId(), userLocation);
        double shippingFee = calculateShippingFee(distanceInMeters);
        BigDecimal roundedShippingFee = BigDecimal.valueOf(shippingFee).setScale(2, RoundingMode.HALF_UP);

        // Calculate total amount including shipping fee
        BigDecimal totalAmount = subtotal.add(roundedShippingFee, new MathContext(18, RoundingMode.HALF_UP));

        return OrderPriceCalculationResponseDTO.builder()
                .subtotal(subtotal)
                .shippingFee(roundedShippingFee)
                .totalAmount(totalAmount)
                .build();
    }
}
