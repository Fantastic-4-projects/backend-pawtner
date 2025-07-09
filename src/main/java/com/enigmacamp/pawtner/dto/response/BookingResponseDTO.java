package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.entity.User;
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
public class BookingResponseDTO {
    private UUID id;
    private UserResponseDTO customer;
    private PetResponseDTO pet;
    private String petName;
    private UUID serviceId;
    private String serviceName;
    private UUID businessId;
    private String businessName;
    private String bookingNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalPrice;
    private String status;
    private String snapToken;
    private String redirectUrl;
    private LocalDateTime createdAt;
}
