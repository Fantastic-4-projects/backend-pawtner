package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.entity.Payment;
import com.enigmacamp.pawtner.repository.PaymentRepository;
import com.enigmacamp.pawtner.service.PaymentService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
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
            // MidtransSnapApi snapApi = new ConfigFactory(config).getSnapApi();

            Map<String, Object> transactionDetails = new HashMap<>();
            transactionDetails.put("order_id", payment.getOrder().getOrderNumber());
            transactionDetails.put("gross_amount", payment.getAmount().longValue());

            Map<String, String> customerDetails = new HashMap<>();
            customerDetails.put("first_name", payment.getOrder().getCustomer().getName());
            customerDetails.put("email", payment.getOrder().getCustomer().getEmail());

            Map<String, Object> params = new HashMap<>();
            params.put("transaction_details", transactionDetails);
            params.put("customer_details", customerDetails);

            String snapToken = midtransSnapApi.createTransactionToken(params);
            payment.setSnapToken(snapToken);
            // Using the order number as the reference ID, as the token can be long
            payment.setPaymentGatewayRefId(payment.getOrder().getOrderNumber());
            return paymentRepository.save(payment);
        } catch (MidtransError e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
