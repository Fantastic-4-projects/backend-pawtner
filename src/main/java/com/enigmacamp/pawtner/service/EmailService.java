package com.enigmacamp.pawtner.service;

public interface EmailService {
    void sendVerificationCodeEmail(String toEmail, String name, String verificationCode);
    void sendPasswordResetEmail(String toEmail, String name, String resetLink);
}
