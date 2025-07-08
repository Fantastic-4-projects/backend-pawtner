package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.constant.ServiceCategory;
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
public class ServiceResponseDTO {
    private UUID id;
    private BusinessResponseDTO business;
    private ServiceCategory category;
    private String name;
    private BigDecimal basePrice;
    private Integer capacityPerDay;
    private String imageUrl;
    private Boolean isActive;
    private List<ReviewResponseDTO> reviews;
    private Double averageRating;
    private Long reviewCount;
}