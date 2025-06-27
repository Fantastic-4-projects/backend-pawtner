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

            helper.setTo(toEmail);
            helper.setSubject("Verifikasi Akun Pawtner Anda.");
            helper.setText(html, true);

            mailSender.send(message);

            logger.info("Verification email sent successfully to {}", toEmail);
        } catch (MessagingException | RuntimeException e) {
            logger.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Gagal mengirim email verifikasi.");
        }
    }
}
