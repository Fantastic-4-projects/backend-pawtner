package com.enigmacamp.pawtner.entity;

import com.enigmacamp.pawtner.constant.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "order_id", columnDefinition = "uuid")
    private Order order;

    @OneToOne
    @JoinColumn(name = "booking_id", columnDefinition = "uuid")
    private Booking booking;

    @NotBlank(message = "Payment gateway reference ID is required")
    @Size(max = 255)
    @Column(unique = true, nullable = false)
    private String paymentGatewayRefId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    @Size(max = 100)
    private String paymentMethod;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Size(max = 255)
    private String snapToken;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Column(columnDefinition = "TEXT")
    private String webhookPayload;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}