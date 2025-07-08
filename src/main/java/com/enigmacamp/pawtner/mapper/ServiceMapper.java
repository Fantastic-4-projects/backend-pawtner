package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.ReviewResponseDTO;
import com.enigmacamp.pawtner.dto.response.ServiceResponseDTO;
import com.enigmacamp.pawtner.entity.Service;

import java.util.Collections;
import java.util.List;

public class ServiceMapper {
    public static ServiceResponseDTO mapToResponse(Service service){
        List<ReviewResponseDTO> reviews = (service.getReviews() != null)
                ? service.getReviews().stream().map(ReviewMapper::mapToResponse).toList()
                : Collections.emptyList();

        return ServiceResponseDTO.builder()
                .id(service.getId())
                .business(BusinessMapper.mapToResponse(service.getBusiness()))
                .category(service.getCategory())
                .name(service.getName())
                .basePrice(service.getBasePrice())
                .capacityPerDay(service.getCapacityPerDay())
                .imageUrl(service.getImageUrl())
                .reviews(reviews)
                .reviewCount(service.getReviewCount())
                .averageRating(service.getAverageRating())
                .isActive(service.getIsActive())
                .build();
    }
}
