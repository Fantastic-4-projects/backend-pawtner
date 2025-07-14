package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.ReviewRequestDTO;
import com.enigmacamp.pawtner.dto.response.ReviewResponseDTO;
import com.enigmacamp.pawtner.entity.*;
import com.enigmacamp.pawtner.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User customer;
    private Business business;
    private Product product;
    private Review review;
    private ReviewRequestDTO reviewRequestDTO;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(UUID.randomUUID()).email("customer@example.com").build();
        business = Business.builder().id(UUID.randomUUID()).name("Test Business").build();
        product = Product.builder().id(UUID.randomUUID()).name("Test Product").build();
        review = Review.builder().id(UUID.randomUUID()).user(customer).business(business).rating(5).comment("Great!").build();
        reviewRequestDTO = ReviewRequestDTO.builder().businessId(business.getId()).rating(5).comment("Great!").build();
    }

    @Test
    @DisplayName("getReviewById should throw exception when not found")
    void getReviewById_shouldFail_whenNotFound() {
        when(reviewRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Review not found");
    }

    @Test
    @DisplayName("deleteReview should succeed if user is the owner of the review")
    void deleteReview_shouldSucceed_whenUserIsOwner() {
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(userRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));

        reviewService.deleteReview(review.getId(), customer.getEmail());

        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("deleteReview should throw exception if user is not the owner")
    void deleteReview_shouldFail_whenUserIsNotOwner() {
        User anotherUser = User.builder().id(UUID.randomUUID()).email("another@user.com").build();
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(userRepository.findByEmail(anotherUser.getEmail())).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> reviewService.deleteReview(review.getId(), anotherUser.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You are not authorized to delete this review");

        verify(reviewRepository, never()).delete(any(Review.class));
    }
}