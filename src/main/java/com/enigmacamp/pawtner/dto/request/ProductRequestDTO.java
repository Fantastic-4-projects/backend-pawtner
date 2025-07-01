package com.enigmacamp.pawtner.dto.request;

import java.util.UUID;
import com.enigmacamp.pawtner.constant.ProductCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ProductRequestDTO {
    private Integer id;

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Product category is required")
    private ProductCategory category;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be non-negative")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock must be non-negative")
    private Integer stockQuantity;

    private MultipartFile image;
}
