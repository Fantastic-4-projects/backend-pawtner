package com.enigmacamp.pawtner.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    @GetMapping("/payment/success")
    public ResponseEntity<Void> redirectAfterPayment(@RequestParam("order_id") String orderId) {
        URI redirectUri = URI.create("pawtner://payment/settlement?order_id=" + orderId);
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }
}
