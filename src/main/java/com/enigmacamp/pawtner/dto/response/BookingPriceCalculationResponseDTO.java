package com.enigmacamp.pawtner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingPriceCalculationResponseDTO {
    private UUID serviceId;
    private BigDecimal basePrice;
    private BigDecimal deliveryFee;
    private BigDecimal totalPrice;
}