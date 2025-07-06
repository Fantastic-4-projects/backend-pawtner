package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.entity.Payment;
import com.enigmacamp.pawtner.repository.PaymentRepository;
import com.enigmacamp.pawtner.service.PaymentService;
import com.midtrans.Config;
import com.midtrans.ConfigFactory;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${midtrans.server-key}")
    private String serverKey;

    @Value("${midtrans.client-key}")
    private String clientKey;

    @Value("${midtrans.is-production}")
    private boolean isProduction;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        try {
            Config config = Config.builder()
                    .setServerKey(serverKey)
                    .setClientKey(clientKey)
                    .setIsProduction(isProduction)
                    .build();

            MidtransSnapApi snapApi = new ConfigFactory(config).getSnapApi();

            Map<String, Object> transactionDetails = new HashMap<>();
            transactionDetails.put("order_id", payment.getOrder().getOrderNumber());
            transactionDetails.put("gross_amount", payment.getAmount().longValue());

            Map<String, String> customerDetails = new HashMap<>();
            customerDetails.put("first_name", payment.getOrder().getCustomer().getName());
            customerDetails.put("email", payment.getOrder().getCustomer().getEmail());

            Map<String, Object> params = new HashMap<>();
            params.put("transaction_details", transactionDetails);
            params.put("customer_details", customerDetails);

            String snapToken = snapApi.createTransactionToken(params);
            payment.setSnapToken(snapToken);
            // Using the order number as the reference ID, as the token can be long
            payment.setPaymentGatewayRefId(payment.getOrder().getOrderNumber());
            return paymentRepository.save(payment);
        } catch (MidtransError e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
