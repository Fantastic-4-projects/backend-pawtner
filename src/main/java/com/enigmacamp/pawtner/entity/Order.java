
package com.enigmacamp.pawtner.entity;

import com.enigmacamp.pawtner.constant.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @NotBlank(message = "Order number is required")
    @Size(max = 255)
    @Column(unique = true, nullable = false)
    private String orderNumber;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private Double totalAmount;

    @Builder.Default
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
