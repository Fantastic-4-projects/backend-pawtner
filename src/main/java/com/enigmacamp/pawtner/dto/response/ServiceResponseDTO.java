package com.enigmacamp.pawtner.dto.response;

import java.util.UUID;
import com.enigmacamp.pawtner.constant.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ServiceResponseDTO {
    private Integer id;
    private UUID businessId;
    private ServiceCategory category;
    private String name;
    private BigDecimal basePrice;
    private Integer capacityPerDay;
    private String imageUrl;
    private Boolean isActive;
}
