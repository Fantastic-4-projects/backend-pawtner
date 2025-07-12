package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.UpdateUserStatusRequestDTO;

public interface EmailService {
    void sendVerificationCodeEmail(String toEmail, String name, String verificationCode);
    void sendPasswordResetEmail(String toEmail, String name, String resetLink);
    void sendBusinessApprovalEmail(String toEmail, String ownerName, String businessName, Boolean isApproved, String reason);
    void sendUserStatusChangeEmail(String toEmail, String userName, String action, boolean statusValue, String reason);
}
