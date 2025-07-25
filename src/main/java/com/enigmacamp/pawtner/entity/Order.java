package com.enigmacamp.pawtner.entity;

import com.enigmacamp.pawtner.constant.OrderStatus;
import com.enigmacamp.pawtner.constant.DeliveryLocationType;
import com.enigmacamp.pawtner.constant.DeliveryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false, columnDefinition = "uuid")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false, columnDefinition = "uuid")
    private Business business;

    @NotBlank(message = "Order number is required")
    @Size(max = 255)
    @Column(unique = true, nullable = false)
    private String orderNumber;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal totalAmount;

    @Digits(integer = 10, fraction = 2)
    private BigDecimal shippingFee;

    @Builder.Default
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    private DeliveryLocationType deliveryLocationType;

    private Double deliveryLatitude;

    private Double deliveryLongitude;

    private String deliveryAddressDetail;

    @ManyToOne
    @JoinColumn(name = "pickup_business_id", columnDefinition = "uuid")
    private Business pickupBusiness;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}