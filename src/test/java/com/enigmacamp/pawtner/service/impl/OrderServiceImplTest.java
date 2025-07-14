package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.DeliveryType;
import com.enigmacamp.pawtner.constant.OrderStatus;
import com.enigmacamp.pawtner.constant.PaymentStatus;
import com.enigmacamp.pawtner.dto.request.OrderRequestDTO;
import com.enigmacamp.pawtner.dto.response.OrderPriceCalculationResponseDTO;
import com.enigmacamp.pawtner.dto.response.OrderResponseDTO;
import com.enigmacamp.pawtner.dto.response.ShoppingCartResponseDTO;
import com.enigmacamp.pawtner.dto.response.CartItemResponseDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.entity.*;
import com.enigmacamp.pawtner.repository.*;
import com.enigmacamp.pawtner.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ShoppingCartService shoppingCartService;
    @Mock
    private UserService userService;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BusinessService businessService;
    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User customer;
    private Business business;
    private Product product;
    private ShoppingCartResponseDTO shoppingCartDTO;
    private OrderRequestDTO orderRequestDTO;
    private Payment payment;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(UUID.randomUUID())
                .email("customer@example.com")
                .build();

        GeometryFactory geometryFactory = new GeometryFactory();
        business = Business.builder()
                .id(UUID.randomUUID())
                .name("Pet Shop A")
                .location(geometryFactory.createPoint(new Coordinate(106.8456, -6.2088)))
                .build();
        business.getLocation().setSRID(4326);

        product = Product.builder()
                .id(UUID.randomUUID())
                .name("Dog Food")
                .price(BigDecimal.valueOf(50000))
                .stockQuantity(10)
                .business(business)
                .build();

        CartItemResponseDTO cartItemDTO = CartItemResponseDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(2)
                .build();

        BusinessResponseDTO businessResponseDTO = BusinessResponseDTO.builder()
                .businessId(business.getId())
                .businessName(business.getName())
                .build();

        shoppingCartDTO = ShoppingCartResponseDTO.builder()
                .id(UUID.randomUUID())
                .customerId(customer.getId())
                .business(businessResponseDTO)
                .items(Collections.singletonList(cartItemDTO))
                .totalPrice(BigDecimal.valueOf(100000)) // 2 * 50000
                .build();

        orderRequestDTO = OrderRequestDTO.builder()
                .deliveryType(DeliveryType.DELIVERY)
                .deliveryLatitude(-6.2100)
                .deliveryLongitude(106.8500)
                .deliveryAddressDetail("Jl. Contoh No. 123")
                .build();

        payment = Payment.builder()
                .id(UUID.randomUUID())
                .amount(BigDecimal.valueOf(100000))
                .status(PaymentStatus.PENDING)
                .snapToken("mockSnapToken")
                .redirectUrl("mockRedirectUrl")
                .build();

        order = Order.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .business(business)
                .orderNumber("ORD-TEST")
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(BigDecimal.valueOf(100000))
                .shippingFee(BigDecimal.ZERO)
                .build();
    }

    @Test
    void createOrderFromCart_delivery_success() {
        when(shoppingCartService.getShoppingCartByCustomerId(anyString())).thenReturn(shoppingCartDTO);
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(customer);
        when(businessService.getBusinessByIdForInternal(any(UUID.class))).thenReturn(business);
        when(businessRepository.calculateDistanceToBusiness(any(UUID.class), any(org.locationtech.jts.geom.Point.class))).thenReturn(5000.0); // 5 km
        when(productService.getProductEntityById(any(UUID.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentService.createPayment(any(Payment.class))).thenReturn(payment);
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));

        OrderResponseDTO result = orderService.createOrderFromCart("customer@example.com", orderRequestDTO);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING_PAYMENT, result.getStatus());
        assertEquals(BigDecimal.valueOf(110000.00).stripTrailingZeros(), result.getTotalAmount().stripTrailingZeros()); // 100000 + 10000 (shipping)
        assertEquals("mockSnapToken", result.getSnapToken());
        assertEquals("mockRedirectUrl", result.getRedirectUrl());

        verify(shoppingCartService, times(1)).clearShoppingCart(anyString());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(paymentService, times(1)).createPayment(any(Payment.class));
    }

    @Test
    void createOrderFromCart_pickup_success() {
        orderRequestDTO.setDeliveryType(DeliveryType.PICKUP);
        orderRequestDTO.setPickupBusinessId(business.getId());

        when(shoppingCartService.getShoppingCartByCustomerId(anyString())).thenReturn(shoppingCartDTO);
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(customer);
        when(businessService.getBusinessByIdForInternal(any(UUID.class))).thenReturn(business);
        when(productService.getProductEntityById(any(UUID.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentService.createPayment(any(Payment.class))).thenReturn(payment);
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));

        OrderResponseDTO result = orderService.createOrderFromCart("customer@example.com", orderRequestDTO);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING_PAYMENT, result.getStatus());
        assertEquals(BigDecimal.valueOf(100000).stripTrailingZeros(), result.getTotalAmount().stripTrailingZeros()); // No shipping fee for pickup
        assertEquals("mockSnapToken", result.getSnapToken());
        assertEquals("mockRedirectUrl", result.getRedirectUrl());

        verify(shoppingCartService, times(1)).clearShoppingCart(anyString());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(paymentService, times(1)).createPayment(any(Payment.class));
        verify(businessRepository, never()).calculateDistanceToBusiness(any(), any()); // Should not call for pickup
    }

    @Test
    void createOrderFromCart_emptyCart_throwsException() {
        shoppingCartDTO.setItems(Collections.emptyList());
        shoppingCartDTO.setTotalPrice(BigDecimal.ZERO);

        when(shoppingCartService.getShoppingCartByCustomerId(anyString())).thenReturn(shoppingCartDTO);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.createOrderFromCart("customer@example.com", orderRequestDTO));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Shopping cart is empty", exception.getReason());
    }

    @Test
    void createOrderFromCart_notEnoughStock_throwsException() {
        product.setStockQuantity(1); // Only 1 in stock, but cart requests 2

        when(shoppingCartService.getShoppingCartByCustomerId(anyString())).thenReturn(shoppingCartDTO);
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(customer);
        when(businessService.getBusinessByIdForInternal(any(UUID.class))).thenReturn(business);
        when(businessRepository.calculateDistanceToBusiness(any(UUID.class), any(org.locationtech.jts.geom.Point.class))).thenReturn(5000.0);
        when(productService.getProductEntityById(any(UUID.class))).thenReturn(product);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.createOrderFromCart("customer@example.com", orderRequestDTO));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Not enough stock for product: Dog Food"));
    }

    @Test
    void createOrderFromCart_pickupBusinessMismatch_throwsException() {
        orderRequestDTO.setDeliveryType(DeliveryType.PICKUP);
        orderRequestDTO.setPickupBusinessId(UUID.randomUUID()); // Different business ID

        Business anotherBusiness = Business.builder().id(UUID.randomUUID()).name("Another Shop").build();

        when(shoppingCartService.getShoppingCartByCustomerId(anyString())).thenReturn(shoppingCartDTO);
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(customer);
        when(businessService.getBusinessByIdForInternal(shoppingCartDTO.getBusiness().getBusinessId())).thenReturn(business);
        when(businessService.getBusinessByIdForInternal(orderRequestDTO.getPickupBusinessId())).thenReturn(anotherBusiness);


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.createOrderFromCart("customer@example.com", orderRequestDTO));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Pickup is only available from the business where the items were purchased.", exception.getReason());
    }

    @Test
    void getOrderById_success() {
        List<OrderItem> orderItems = Collections.singletonList(OrderItem.builder().order(order).product(product).quantity(2).pricePerUnit(product.getPrice()).build());
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder(any(Order.class))).thenReturn(orderItems);
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));

        OrderResponseDTO result = orderService.getOrderById(order.getId());

        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
        assertEquals(order.getOrderNumber(), result.getOrderNumber());
        assertEquals(1, result.getItems().size());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(orderItemRepository, times(1)).findByOrder(any(Order.class));
    }

    @Test
    void getOrderById_notFound_throwsException() {
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.getOrderById(UUID.randomUUID()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Order not found", exception.getReason());
    }

    @Test
    void getAllOrdersByCustomerId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Collections.singletonList(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        List<OrderItem> orderItems = Collections.singletonList(OrderItem.builder().order(order).product(product).quantity(2).pricePerUnit(product.getPrice()).build());

        when(userService.getUserByEmailForInternal(anyString())).thenReturn(customer);
        when(orderRepository.findByCustomer(any(User.class), any(Pageable.class))).thenReturn(orderPage);
        when(orderItemRepository.findByOrder(any(Order.class))).thenReturn(orderItems);
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));

        Page<OrderResponseDTO> result = orderService.getAllOrdersByCustomerId("customer@example.com", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(order.getId(), result.getContent().get(0).getId());
        verify(userService, times(1)).getUserByEmailForInternal(anyString());
        verify(orderRepository, times(1)).findByCustomer(any(User.class), any(Pageable.class));
    }

    @Test
    void handleWebhook_captureAccept_statusProcessing() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", order.getOrderNumber());
        payload.put("transaction_status", "capture");
        payload.put("fraud_status", "accept");

        order.setStatus(OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        orderService.handleWebhook(payload);

        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        assertEquals(PaymentStatus.CAPTURE, payment.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void handleWebhook_settlement_statusProcessing() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", order.getOrderNumber());
        payload.put("transaction_status", "settlement");
        payload.put("fraud_status", "accept"); // Fraud status doesn't matter for settlement

        order.setStatus(OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        orderService.handleWebhook(payload);

        assertEquals(OrderStatus.PROCESSING, order.getStatus());
        assertEquals(PaymentStatus.SETTLEMENT, payment.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void handleWebhook_cancel_statusCancelled() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", order.getOrderNumber());
        payload.put("transaction_status", "cancel");
        payload.put("fraud_status", "accept");

        order.setStatus(OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        orderService.handleWebhook(payload);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(PaymentStatus.CANCEL, payment.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void handleWebhook_pending_statusPendingPayment() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", order.getOrderNumber());
        payload.put("transaction_status", "pending");
        payload.put("fraud_status", "accept");

        order.setStatus(OrderStatus.PROCESSING); // Change initial status to ensure save is called
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        orderService.handleWebhook(payload);

        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void handleWebhook_orderNotFound_throwsException() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", "NON_EXISTENT_ORDER");
        payload.put("transaction_status", "settlement");
        payload.put("fraud_status", "accept");

        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.handleWebhook(payload));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Order not found", exception.getReason());
    }

    @Test
    void updateOrderStatus_success() {
        Business ownerBusiness = Business.builder().id(business.getId()).build();
        List<OrderItem> orderItems = Collections.singletonList(OrderItem.builder().order(order).product(product).quantity(2).pricePerUnit(product.getPrice()).build());

        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(businessService.getBusinessByOwnerEmailForInternal(anyString())).thenReturn(ownerBusiness);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrder(any(Order.class))).thenReturn(orderItems);
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));


        OrderResponseDTO result = orderService.updateOrderStatus(order.getId(), "COMPLETED", "owner@example.com");

        assertNotNull(result);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrderStatus_orderNotFound_throwsException() {
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.updateOrderStatus(UUID.randomUUID(), "COMPLETED", "owner@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Order not found", exception.getReason());
    }

    @Test
    void updateOrderStatus_forbidden_throwsException() {
        Business anotherBusiness = Business.builder().id(UUID.randomUUID()).build(); // Different business
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(businessService.getBusinessByOwnerEmailForInternal(anyString())).thenReturn(anotherBusiness);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.updateOrderStatus(order.getId(), "COMPLETED", "owner@example.com"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Order does not belong to this business owner", exception.getReason());
    }

    @Test
    void updateOrderStatus_invalidStatus_throwsException() {
        Business ownerBusiness = Business.builder().id(business.getId()).build();
        when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(order));
        when(businessService.getBusinessByOwnerEmailForInternal(anyString())).thenReturn(ownerBusiness);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.updateOrderStatus(order.getId(), "INVALID_STATUS", "owner@example.com"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid order status: INVALID_STATUS", exception.getReason());
    }

    @Test
    void getAllOrdersByBusinessId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Collections.singletonList(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        List<OrderItem> orderItems = Collections.singletonList(OrderItem.builder().order(order).product(product).quantity(2).pricePerUnit(product.getPrice()).build());

        when(businessService.getBusinessByIdForInternal(any(UUID.class))).thenReturn(business);
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(orderPage);
        when(orderItemRepository.findByOrder(any(Order.class))).thenReturn(orderItems);
        when(paymentRepository.findByOrder(any(Order.class))).thenReturn(Optional.of(payment));


        Page<OrderResponseDTO> result = orderService.getAllOrdersByBusinessId(
                business.getId(), null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(order.getId(), result.getContent().get(0).getId());
        verify(businessService, times(1)).getBusinessByIdForInternal(any(UUID.class));
        verify(orderRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void calculateShippingFee_success() {
        double distanceInMeters = 5000; // 5 km
        double expectedFee = 5000 + (1000 * 5); // Base fee + 5km * 1000
        double actualFee = orderService.calculateShippingFee(distanceInMeters);
        assertEquals(expectedFee, actualFee, 0.001);
    }

    @Test
    void calculateOrderPrice_delivery_success() {
        when(shoppingCartService.getShoppingCartByCustomerId(anyString())).thenReturn(shoppingCartDTO);
        when(businessService.getBusinessByIdForInternal(any(UUID.class))).thenReturn(business);
        when(businessRepository.calculateDistanceToBusiness(any(UUID.class), any(org.locationtech.jts.geom.Point.class))).thenReturn(5000.0); // 5 km

        OrderPriceCalculationResponseDTO result = orderService.calculateOrderPrice("customer@example.com", orderRequestDTO);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100000).stripTrailingZeros(), result.getSubtotal().stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(10000.00).stripTrailingZeros(), result.getShippingFee().stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(110000.00).stripTrailingZeros(), result.getTotalAmount().stripTrailingZeros());
    }

    @Test
    void calculateOrderPrice_pickup_success() {
        orderRequestDTO.setDeliveryType(DeliveryType.PICKUP);
        orderRequestDTO.setPickupBusinessId(business.getId());

        when(shoppingCartService.getShoppingCartByCustomerId(anyString())).thenReturn(shoppingCartDTO);
        when(businessService.getBusinessByIdForInternal(any(UUID.class))).thenReturn(business);

        OrderPriceCalculationResponseDTO result = orderService.calculateOrderPrice("customer@example.com", orderRequestDTO);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100000).stripTrailingZeros(), result.getSubtotal().stripTrailingZeros());
        assertEquals(BigDecimal.ZERO.stripTrailingZeros(), result.getShippingFee().stripTrailingZeros());
        assertEquals(BigDecimal.valueOf(100000).stripTrailingZeros(), result.getTotalAmount().stripTrailingZeros());
        verify(businessRepository, never()).calculateDistanceToBusiness(any(), any()); // Should not call for pickup
    }

    @Test
    void calculateOrderPrice_emptyCart_throwsException() {
        shoppingCartDTO.setItems(Collections.emptyList());
        shoppingCartDTO.setTotalPrice(BigDecimal.ZERO);

        when(shoppingCartService.getShoppingCartByCustomerId(anyString())).thenReturn(shoppingCartDTO);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.calculateOrderPrice("customer@example.com", orderRequestDTO));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Shopping cart is empty", exception.getReason());
    }

    @Test
    void calculateOrderPrice_pickupBusinessMismatch_throwsException() {
        orderRequestDTO.setDeliveryType(DeliveryType.PICKUP);
        orderRequestDTO.setPickupBusinessId(UUID.randomUUID()); // Different business ID

        when(shoppingCartService.getShoppingCartByCustomerId(anyString())).thenReturn(shoppingCartDTO);
        when(businessService.getBusinessByIdForInternal(shoppingCartDTO.getBusiness().getBusinessId())).thenReturn(business);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                orderService.calculateOrderPrice("customer@example.com", orderRequestDTO));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Pickup is only available for the business where the items are from.", exception.getReason());
    }
}
