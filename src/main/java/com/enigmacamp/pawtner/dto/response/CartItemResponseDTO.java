package com.enigmacamp.pawtner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponseDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal subTotal;
}
