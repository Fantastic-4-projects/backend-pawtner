package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.entity.User;
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
public class BookingResponseDTO {
    private UUID id;
    private UserResponseDTO customer;
    private PetResponseDTO pet;
    private String petName;
    private UUID serviceId;
    private String serviceName;
    private String serviceImageUrl;
    private UUID businessId;
    private String businessName;
    private String bookingNumber;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private BigDecimal totalPrice;
    private String status;
    private String snapToken;
    private String redirectUrl;
    private String deliveryType;
    private String deliveryAddress;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String businessAddress;
    private ZonedDateTime createdAt;
}
