package com.enigmacamp.pawtner.config;

import com.midtrans.Config;
import com.midtrans.service.MidtransCoreApi;
import com.midtrans.service.MidtransSnapApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MidtransConfig {

    @Value("${midtrans.server-key}")
    private String serverKey;

    @Value("${midtrans.client-key}")
    private String clientKey;

    @Value("${midtrans.is-production}")
    private boolean isProduction;

    @Bean
    public Config midtransConfigProperties() {
        return Config.builder()
                .setServerKey(serverKey)
                .setClientKey(clientKey)
                .setIsProduction(isProduction)
                .build();
    }

    @Bean
    public MidtransSnapApi midtransSnapApi(Config midtransConfigProperties) {
        return new com.midtrans.ConfigFactory(midtransConfigProperties).getSnapApi();
    }

    @Bean
    public MidtransCoreApi midtransCoreApi(Config midtransConfigProperties) {
        return new com.midtrans.ConfigFactory(midtransConfigProperties).getCoreApi();
    }
}