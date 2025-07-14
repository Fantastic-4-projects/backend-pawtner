package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.entity.Order;
import com.enigmacamp.pawtner.entity.Payment;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.PaymentRepository;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MidtransSnapApi midtransSnapApi;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private User customer;
    private Order order;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setName("John Doe");
        customer.setEmail("john.doe@example.com");

        order = new Order();
        order.setOrderNumber("ORDER-123");
        order.setCustomer(customer);

        booking = new Booking();
        booking.setBookingNumber("BOOKING-456");
        booking.setCustomer(customer);

        payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(100000));
    }

    @Test
    void createPayment_shouldReturnPaymentWithSnapTokenAndRedirectUrl_whenOrderIsProvided() throws MidtransError, JSONException {
        payment.setOrder(order);

        JSONObject midtransResponse = new JSONObject();
        midtransResponse.put("token", "mockSnapToken");
        midtransResponse.put("redirect_url", "http://mock.redirect.url/order");

        when(midtransSnapApi.createTransaction(any())).thenReturn(midtransResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.createPayment(payment);

        assertNotNull(result);
        assertEquals("mockSnapToken", result.getSnapToken());
        assertEquals("ORDER-123", result.getPaymentGatewayRefId());
        assertEquals("http://mock.redirect.url/order", result.getRedirectUrl());

        verify(midtransSnapApi, times(1)).createTransaction(any());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_shouldReturnPaymentWithSnapTokenAndRedirectUrl_whenBookingIsProvided() throws MidtransError, JSONException {
        payment.setBooking(booking);

        JSONObject midtransResponse = new JSONObject();
        midtransResponse.put("token", "mockSnapToken");
        midtransResponse.put("redirect_url", "http://mock.redirect.url/booking");

        when(midtransSnapApi.createTransaction(any())).thenReturn(midtransResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.createPayment(payment);

        assertNotNull(result);
        assertEquals("mockSnapToken", result.getSnapToken());
        assertEquals("BOOKING-456", result.getPaymentGatewayRefId());
        assertEquals("http://mock.redirect.url/booking", result.getRedirectUrl());

        verify(midtransSnapApi, times(1)).createTransaction(any());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_shouldThrowRuntimeException_whenMidtransErrorOccurs() throws MidtransError {
        payment.setOrder(order);

        when(midtransSnapApi.createTransaction(any())).thenThrow(new MidtransError("Midtrans API Error"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> paymentService.createPayment(payment));

        assertTrue(thrown.getMessage().contains("Failed to create Midtrans transaction token"));
        verify(midtransSnapApi, times(1)).createTransaction(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_shouldThrowIllegalStateException_whenNeitherOrderNorBookingIsProvided() throws MidtransError {
        payment.setOrder(null);
        payment.setBooking(null);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> paymentService.createPayment(payment));

        assertEquals("Payment must be associated with an Order or a Booking.", thrown.getMessage());
        verify(midtransSnapApi, never()).createTransaction(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
