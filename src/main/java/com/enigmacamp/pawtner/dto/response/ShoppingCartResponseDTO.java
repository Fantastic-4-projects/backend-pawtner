package com.enigmacamp.pawtner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShoppingCartResponseDTO {
    private UUID id;
    private UUID customerId;
    private BusinessResponseDTO business;
    private List<CartItemResponseDTO> items;
    private BigDecimal totalPrice;
}
