package com.enigmacamp.pawtner.entity;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.constant.DeliveryLocationType;
import com.enigmacamp.pawtner.constant.DeliveryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false, columnDefinition = "uuid")
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @NotBlank(message = "Booking number is required")
    @Size(max = 255)
    @Column(unique = true, nullable = false)
    private String bookingNumber;

    @NotNull(message = "Start time is required")
    private ZonedDateTime startTime;

    @NotNull(message = "End time is required")
    private ZonedDateTime endTime;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 18, fraction = 2)
    private BigDecimal totalPrice;

    @Builder.Default
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.REQUESTED;

    @Column(name = "snap_token")
    private String snapToken;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    private Payment payment;

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

    @Builder.Default
    @Column(updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = ZonedDateTime.now();
        }
    }
}