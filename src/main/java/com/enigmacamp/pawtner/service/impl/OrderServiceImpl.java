package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.DeliveryType;
import com.enigmacamp.pawtner.constant.OrderStatus;
import com.enigmacamp.pawtner.constant.PaymentStatus;
import com.enigmacamp.pawtner.dto.request.OrderRequestDTO;
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


import com.enigmacamp.pawtner.specification.OrderSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final PaymentRepository paymentRepository;
    private final BusinessService businessService;
    private final BusinessRepository businessRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();


    @Override
    @Transactional
    public OrderResponseDTO createOrderFromCart(String customerEmail,
                                                OrderRequestDTO orderRequestDTO) {
        ShoppingCartResponseDTO shoppingCartDTO = shoppingCartService.getShoppingCartByCustomerId(customerEmail);

        if (shoppingCartDTO.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");
        }

        User customer = userService.getUserByEmailForInternal(customerEmail);
        // This business is the one associated with all items in the cart.
        Business cartBusiness = businessService.getBusinessByIdForInternal(shoppingCartDTO.getBusiness().getBusinessId());

        Order.OrderBuilder orderBuilder = Order.builder()
                .customer(customer)
                .business(cartBusiness) // The business that owns the products
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING_PAYMENT);

        BigDecimal shippingFee = BigDecimal.ZERO;
        Business pickupBusiness;

        if (orderRequestDTO.getDeliveryType() == com.enigmacamp.pawtner.constant.DeliveryType.DELIVERY) {
            Point userLocation = geometryFactory.createPoint(new Coordinate(orderRequestDTO.getDeliveryLongitude(), orderRequestDTO.getDeliveryLatitude()));
            userLocation.setSRID(4326);
            Double distanceInMeters = businessRepository.calculateDistanceToBusiness(cartBusiness.getId(), userLocation);
            shippingFee = BigDecimal.valueOf(calculateShippingFee(distanceInMeters)).setScale(2, RoundingMode.HALF_UP);

            orderBuilder.deliveryType(com.enigmacamp.pawtner.constant.DeliveryType.DELIVERY)
                    .deliveryLocationType(orderRequestDTO.getDeliveryLocationType())
                    .deliveryLatitude(orderRequestDTO.getDeliveryLatitude())
                    .deliveryLongitude(orderRequestDTO.getDeliveryLongitude())
                    .deliveryAddressDetail(orderRequestDTO.getDeliveryAddressDetail());

        } else if (orderRequestDTO.getDeliveryType() == com.enigmacamp.pawtner.constant.DeliveryType.PICKUP) {
            pickupBusiness = businessService.getBusinessByIdForInternal(orderRequestDTO.getPickupBusinessId());
            // Validate that the pickup location is the same as the cart's business
            if (!cartBusiness.getId().equals(pickupBusiness.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pickup is only available from the business where the items were purchased.");
            }
            orderBuilder.deliveryType(com.enigmacamp.pawtner.constant.DeliveryType.PICKUP)
                        .pickupBusiness(pickupBusiness);
        }

        BigDecimal totalAmountWithShipping = shoppingCartDTO.getTotalPrice().add(shippingFee, new MathContext(18, RoundingMode.HALF_UP));
        orderBuilder.totalAmount(totalAmountWithShipping).shippingFee(shippingFee);

        List<OrderItem> orderItems = shoppingCartDTO.getItems().stream().map(cartItemDTO -> {
            Product product = productService.getProductEntityById(cartItemDTO.getProductId());
            if (product.getStockQuantity() < cartItemDTO.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItemDTO.getQuantity());
            // productService.updateProduct(product.getId(), null); // Persist stock change

            return OrderItem.builder()
                    .product(product)
                    .quantity(cartItemDTO.getQuantity())
                    .pricePerUnit(cartItemDTO.getProductPrice())
                    .build();
        }).collect(Collectors.toList());

        Order order = orderBuilder.build();
        orderRepository.save(order);

        orderItems.forEach(orderItem -> {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        });

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentService.createPayment(payment);

        shoppingCartService.clearShoppingCart(customerEmail);

        OrderResponseDTO responseDTO = OrderMapper.mapToResponse(order, orderItems, paymentRepository, shippingFee);
        responseDTO.setSnapToken(payment.getSnapToken());
        responseDTO.setRedirectUrl(payment.getRedirectUrl());
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

        }
    }

    private static OrderStatus getOrderStatus(OrderStatus oldStatus, String transactionStatus, String fraudStatus) {
        OrderStatus newStatus = oldStatus;

        if (transactionStatus.equals("capture")) {
            if (fraudStatus.equals("accept")) {
                newStatus = OrderStatus.PROCESSING;
            }
        } else if (transactionStatus.equals("settlement")) {
            newStatus = OrderStatus.PROCESSING;
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

            return OrderMapper.mapToResponse(order, orderItemRepository.findByOrder(order), paymentRepository, order.getShippingFee());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid order status: " + newStatus);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrdersByBusinessId(
            UUID businessId, String orderNumber, String nameCustomer, String emailCustomer, OrderStatus orderStatus, Pageable pageable
    ) {
        businessService.getBusinessByIdForInternal(businessId);
        Specification<Order> spec = OrderSpecification.getSpecificationByBusiness(businessId, orderNumber, nameCustomer, emailCustomer, orderStatus);
        Page<Order> orders = orderRepository.findAll(spec, pageable);
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
    public OrderPriceCalculationResponseDTO calculateOrderPrice(String customerEmail, OrderRequestDTO orderRequestDTO) {
        ShoppingCartResponseDTO shoppingCartDTO = shoppingCartService.getShoppingCartByCustomerId(customerEmail);

        if (shoppingCartDTO.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");
        }

        BigDecimal subtotal = shoppingCartDTO.getTotalPrice();
        Business business = businessService.getBusinessByIdForInternal(shoppingCartDTO.getBusiness().getBusinessId());

        BigDecimal shippingFee = BigDecimal.ZERO;
        if (orderRequestDTO.getDeliveryType() == DeliveryType.DELIVERY) {
            Point userLocation = geometryFactory.createPoint(new Coordinate(orderRequestDTO.getDeliveryLongitude(), orderRequestDTO.getDeliveryLatitude()));
            userLocation.setSRID(4326);
            Double distanceInMeters = businessRepository.calculateDistanceToBusiness(business.getId(), userLocation);
            shippingFee = BigDecimal.valueOf(calculateShippingFee(distanceInMeters)).setScale(2, RoundingMode.HALF_UP);
        } else if (orderRequestDTO.getDeliveryType() == DeliveryType.PICKUP) {
            if (!shoppingCartDTO.getBusiness().getBusinessId().equals(orderRequestDTO.getPickupBusinessId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pickup is only available for the business where the items are from.");
            }
        }

        BigDecimal totalAmount = subtotal.add(shippingFee, new MathContext(18, RoundingMode.HALF_UP));

        return OrderPriceCalculationResponseDTO.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .totalAmount(totalAmount)
                .build();
    }
}
