package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.ReviewRequestDTO;
import com.enigmacamp.pawtner.dto.response.ReviewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    ReviewResponseDTO createReview(ReviewRequestDTO requestDTO, String customerEmail);
    Page<ReviewResponseDTO> getAllReviews(Pageable pageable);
    ReviewResponseDTO getReviewById(UUID id);
    ReviewResponseDTO updateReview(UUID id, ReviewRequestDTO requestDTO, String customerEmail);
    void deleteReview(UUID id, String customerEmail);
}
