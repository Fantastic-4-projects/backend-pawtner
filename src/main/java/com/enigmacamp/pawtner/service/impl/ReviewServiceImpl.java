package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.ReviewRequestDTO;
import com.enigmacamp.pawtner.dto.response.ReviewResponseDTO;
import com.enigmacamp.pawtner.entity.Review;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.mapper.ReviewMapper;
import com.enigmacamp.pawtner.repository.*;
import com.enigmacamp.pawtner.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Review review = Review.builder()
                .user(customer)
                .rating(requestDTO.getRating())
                .comment(requestDTO.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        if (requestDTO.getBusinessId() != null) {
            review.setBusiness(businessRepository.findById(requestDTO.getBusinessId()).orElse(null));
        }
        if (requestDTO.getProductId() != null) {
            review.setProduct(productRepository.findById(requestDTO.getProductId()).orElse(null));
        }
        if (requestDTO.getServiceId() != null) {
            review.setService(serviceRepository.findById(requestDTO.getServiceId()).orElse(null));
        }

        reviewRepository.save(review);
        return ReviewMapper.mapToResponse(review);
    }

    @Override
    public Page<ReviewResponseDTO> getAllReviews(Authentication authentication, Pageable pageable) {
        User user = (User) authentication.getPrincipal();

        if (user.getRole().name().equals("CUSTOMER")) {
            return reviewRepository.findByUser(user, pageable).map(ReviewMapper::mapToResponse);
        } else if (user.getRole().name().equals("BUSINESS_OWNER")) {
            List<com.enigmacamp.pawtner.entity.Business> businesses = businessRepository.findAllByOwner_Id(user.getId());
            if (businesses.isEmpty()) {
                return Page.empty(pageable);
            }
            return reviewRepository.findByBusinessIn(businesses, pageable).map(ReviewMapper::mapToResponse);
        } else if (user.getRole().name().equals("ADMIN")) {
            return reviewRepository.findAll(pageable).map(ReviewMapper::mapToResponse);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    @Override
    public ReviewResponseDTO getReviewById(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        return ReviewMapper.mapToResponse(review);
    }

    @Override
    public ReviewResponseDTO updateReview(UUID id, ReviewRequestDTO requestDTO, String customerEmail) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (!review.getUser().getEmail().equals(customerEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this review");
        }

        review.setRating(requestDTO.getRating());
        review.setComment(requestDTO.getComment());
        reviewRepository.save(review);
        return ReviewMapper.mapToResponse(review);
    }

    @Override
    public void deleteReview(UUID id, String customerEmail) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        User user = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this review");
        }
        reviewRepository.delete(review);
    }
}