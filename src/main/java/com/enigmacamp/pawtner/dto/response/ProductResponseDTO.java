package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.constant.ProductCategory;
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
public class ProductResponseDTO {
    private UUID id;
    private UUID businessId;
    private String name;
    private ProductCategory category;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean isActive;
}