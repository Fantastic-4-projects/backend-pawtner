package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendVerificationCodeEmail(String toEmail, String name, String verificationCode) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verificationCode", verificationCode);

            String html = templateEngine.process("verification-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("Pawtner <official.pawtner.no.reply@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Verify your Pawtner Account.");
            helper.setText(html, true);

            mailSender.send(message);

            logger.info("Verification email sent successfully to {}", toEmail);
        } catch (MessagingException | RuntimeException e) {
            logger.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Gagal mengirim email verifikasi.");
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String name, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", resetLink);

            String html = templateEngine.process("password-reset-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("Pawtner <official.pawtner.no.reply@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject("Instructions to Reset Your Pawtner Account Password.");
            helper.setText(html, true);

            mailSender.send(message);

            logger.info("Password reset email sent successfully to {}", toEmail);
        } catch (MessagingException | RuntimeException e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Gagal mengirim email reset password.");
        }
    }

    @Override
    public void sendBusinessApprovalEmail(String toEmail, String ownerName, String businessName, Boolean isApproved, String reason) {
        try {
            Context context = new Context();
            context.setVariable("ownerName", ownerName);
            context.setVariable("businessName", businessName);
            context.setVariable("isApproved", isApproved); // isApproved can be null
            context.setVariable("reason", reason); // reason may be null in pending case

            String html = templateEngine.process("business-approval-email", context);

            String subject;
            if (Boolean.TRUE.equals(isApproved)) {
                subject = "Congratulations! Your Business Registration on Pawtner Has Been Approved";
            } else if (Boolean.FALSE.equals(isApproved)) {
                subject = "Update on Your Business Registration Status on Pawtner";
            } else {
                // PENDING CASE (isApproved == null)
                subject = "Your Business Registration is Under Review by the Pawtner Team";
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("Pawtner <official.pawtner.no.reply@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

            logger.info("Business status email sent successfully to {} with status: {}", toEmail, isApproved);
        } catch (MessagingException | RuntimeException e) {
            logger.error("Failed to send business approval email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send business approval status email.");
        }
    }

    @Override
    public void sendUserStatusChangeEmail(String toEmail, String userName, String action, boolean statusValue, String reason) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("action", action);
            context.setVariable("statusValue", statusValue);
            context.setVariable("reason", reason);

            String html = templateEngine.process("user-status-change-email", context);
            String subject = "Important Updates Regarding Your Pawtner Account Status.";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("Pawtner <official.pawtner.no.reply@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

            logger.info("User status change email sent successfully to {}", toEmail);
        } catch (MessagingException | RuntimeException e) {
            logger.error("Failed to send user status change email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Gagal mengirim email perubahan status pengguna.");
        }
    }
}