package com.enigmacamp.pawtner.dto.request;

import com.enigmacamp.pawtner.constant.DeliveryLocationType;
import com.enigmacamp.pawtner.constant.DeliveryType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
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
public class BookingRequestDTO {
    @NotNull(message = "Pet ID is required")
    private UUID petId;

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private ZonedDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private ZonedDateTime endTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private DeliveryType deliveryType;
    private DeliveryLocationType deliveryLocationType;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String deliveryAddressDetail;
    private UUID pickupBusinessId;
}