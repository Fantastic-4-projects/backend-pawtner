package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.ReviewResponseDTO;
import com.enigmacamp.pawtner.entity.Review;

public class ReviewMapper {
    public static ReviewResponseDTO mapToResponse(Review review){
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .user(UserMapper.mapToResponse(review.getUser()))
                .business(BusinessMapper.mapToResponse(review.getBusiness()))
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .serviceId(review.getService() != null ? review.getService().getId() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
