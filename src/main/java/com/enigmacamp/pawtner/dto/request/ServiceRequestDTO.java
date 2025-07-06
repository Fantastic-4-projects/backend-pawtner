package com.enigmacamp.pawtner.dto.request;

import com.enigmacamp.pawtner.constant.ServiceCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequestDTO {
    private UUID id;

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    @NotNull(message = "Service category is required")
    private ServiceCategory category;

    @NotBlank(message = "Service name is required")
    private String name;

    @NotNull(message = "Base price is required")
    @Min(value = 0, message = "Price must be non-negative")
    private BigDecimal basePrice;

    @Min(value = 0, message = "Capacity must be non-negative")
    private Integer capacityPerDay;

    private MultipartFile image;
}
