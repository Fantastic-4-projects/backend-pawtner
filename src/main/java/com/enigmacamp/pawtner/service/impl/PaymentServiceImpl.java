package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.entity.Payment;
import com.enigmacamp.pawtner.repository.PaymentRepository;
import com.enigmacamp.pawtner.service.PaymentService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final MidtransSnapApi midtransSnapApi;

    public PaymentServiceImpl(PaymentRepository paymentRepository, MidtransSnapApi midtransSnapApi) {
        this.paymentRepository = paymentRepository;
        this.midtransSnapApi = midtransSnapApi;
    }

    @Override
    public Payment createPayment(Payment payment) {
        try {
            Map<String, Object> transactionDetails = new HashMap<>();
            String orderId;
            String customerName;
            String customerEmail;

            if (payment.getOrder() != null) {
                orderId = payment.getOrder().getOrderNumber();
                customerName = payment.getOrder().getCustomer().getName();
                customerEmail = payment.getOrder().getCustomer().getEmail();
            } else if (payment.getBooking() != null) {
                orderId = payment.getBooking().getBookingNumber();
                customerName = payment.getBooking().getCustomer().getName();
                customerEmail = payment.getBooking().getCustomer().getEmail();
            } else {
                throw new IllegalStateException("Payment must be associated with an Order or a Booking.");
            }

            transactionDetails.put("order_id", orderId);
            transactionDetails.put("gross_amount", payment.getAmount().longValue());

            Map<String, String> customerDetails = new HashMap<>();
            customerDetails.put("first_name", customerName);
            customerDetails.put("email", customerEmail);
            // Assuming you have a way to get last name, e.g., from customer object
            // customerDetails.put("last_name", customerLastName); 

            Map<String, Object> params = new HashMap<>();
            params.put("transaction_details", transactionDetails);
            params.put("customer_details", customerDetails);

            Map<String, String> callbacks = new HashMap<>();
            callbacks.put("finish", "pawtner://payment/success?order_id=" + orderId);
            callbacks.put("error", "pawtner://payment/failed?order_id=" + orderId);
            callbacks.put("unfinish", "pawtner://payment/unfinish?order_id=" + orderId);
            params.put("callbacks", callbacks);
            

            

            // Dapatkan respons dari Midtrans
        JSONObject response = midtransSnapApi.createTransaction(params); // Gunakan createTransaction untuk mendapatkan redirect_url
        String snapToken = response.getString("token");
        String redirectUrl = response.getString("redirect_url");

        System.out.println("Generated Snap Token: " + snapToken);
        System.out.println("Midtrans Redirect URL: " + redirectUrl);

        payment.setSnapToken(snapToken);
        payment.setPaymentGatewayRefId(orderId);
        payment.setRedirectUrl(redirectUrl); // Simpan redirectUrl di entity Payment
        Payment savedPayment = paymentRepository.save(payment);

        System.out.println("Saved Payment with Redirect URL: " + redirectUrl);
        return savedPayment;
        } catch (MidtransError e) {
            System.err.println("Midtrans Error: " + e.getMessage());
            System.err.println("Midtrans HTTP Status Code: " + e.getStatusCode());
            throw new RuntimeException("Failed to create Midtrans transaction token: " + e.getMessage(), e);
        }
    }
}
