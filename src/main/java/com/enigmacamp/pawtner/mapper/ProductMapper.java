package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.ProductResponseDTO;
import com.enigmacamp.pawtner.dto.response.ReviewResponseDTO;
import com.enigmacamp.pawtner.entity.Product;

import java.util.Collections;
import java.util.List;

public class ProductMapper {
    public static ProductResponseDTO mapToResponse(Product product) {
        List<ReviewResponseDTO> reviews = (product.getReviews() != null)
                ? product.getReviews().stream().map(ReviewMapper::mapToResponse).toList()
                : Collections.emptyList();

        return ProductResponseDTO.builder()
                .id(product.getId())
                .business(BusinessMapper.mapToResponse(product.getBusiness()))
                .name(product.getName())
                .category(product.getCategory())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .reviews(reviews)
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .isActive(product.getIsActive())
                .build();
    }
}
