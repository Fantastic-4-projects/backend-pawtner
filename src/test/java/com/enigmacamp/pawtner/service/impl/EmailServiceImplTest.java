package com.enigmacamp.pawtner.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    @DisplayName("sendVerificationCodeEmail should send email with correct content")
    void sendVerificationCodeEmail_shouldSucceed() throws MessagingException {
        // Given
        String toEmail = "test@example.com";
        String name = "Tester";
        String code = "123456";
        String htmlContent = "<html><body>Verification code is 123456</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("verification-email"), any(Context.class))).thenReturn(htmlContent);

        // When
        emailService.sendVerificationCodeEmail(toEmail, name, code);

        // Then
        verify(mailSender).send(mimeMessage);

        // Use ArgumentCaptor to check what was passed to templateEngine
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("verification-email"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();

        assertThat(capturedContext.getVariable("name")).isEqualTo(name);
        assertThat(capturedContext.getVariable("verificationCode")).isEqualTo(code);
    }

    @Test
    @DisplayName("sendPasswordResetEmail should send email with correct reset link")
    void sendPasswordResetEmail_shouldSucceed() throws MessagingException {
        // Given
        String toEmail = "reset@example.com";
        String name = "Reset User";
        String resetLink = "http://localhost/reset?token=xyz";
        String htmlContent = "<html><body>Reset link</body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("password-reset-email"), any(Context.class))).thenReturn(htmlContent);

        // When
        emailService.sendPasswordResetEmail(toEmail, name, resetLink);

        // Then
        verify(mailSender).send(mimeMessage);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("password-reset-email"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();

        assertThat(capturedContext.getVariable("name")).isEqualTo(name);
        assertThat(capturedContext.getVariable("resetLink")).isEqualTo(resetLink);
    }
}