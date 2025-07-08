package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import com.enigmacamp.pawtner.entity.Service;

public class ServiceMapper {
    public static ServiceResponseDTO mapToResponse(Service service){
        return ServiceResponseDTO.builder()
                .id(service.getId())
                .business(BusinessMapper.mapToResponse(service.getBusiness()))
                .category(service.getCategory())
                .name(service.getName())
                .basePrice(service.getBasePrice())
                .capacityPerDay(service.getCapacityPerDay())
                .imageUrl(service.getImageUrl())
                .reviewCount(service.getReviewCount())
                .averageRating(service.getAverageRating())
                .isActive(service.getIsActive())
                .build();
    }
}
