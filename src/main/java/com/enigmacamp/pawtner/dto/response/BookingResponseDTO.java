package com.enigmacamp.pawtner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponseDTO {
    private UUID id;
    private UUID customerId;
    private UUID petId;
    private String petName;
    private UUID serviceId;
    private String serviceName;
    private UUID businessId;
    private String businessName;
    private String bookingNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalPrice;
    private String status;
    private LocalDateTime createdAt;
}
