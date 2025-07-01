package com.enigmacamp.pawtner.dto.response;

import java.util.UUID;
import com.enigmacamp.pawtner.constant.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ProductResponseDTO {
    private Integer id;
    private UUID businessId;
    private String name;
    private ProductCategory category;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean isActive;
}
