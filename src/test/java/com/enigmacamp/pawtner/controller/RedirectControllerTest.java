package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RedirectController.class)
@AutoConfigureMockMvc(addFilters = false)
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Test
    @DisplayName("GET /payment/success should redirect to deep link with settlement status")
    void redirectAfterPayment_shouldRedirectToDeepLink() throws Exception {
        String orderId = "ORD-12345";
        String expectedRedirectUrl = "pawtner://payment/settlement?order_id=" + orderId;

        mockMvc.perform(get("/payment/success")
                        .param("order_id", orderId))
                .andExpect(status().isFound()) // 302 Found
                .andExpect(header().string(HttpHeaders.LOCATION, expectedRedirectUrl));
    }
}