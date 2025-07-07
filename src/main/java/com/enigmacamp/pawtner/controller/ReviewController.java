package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.ReviewRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.ReviewResponseDTO;
import com.enigmacamp.pawtner.service.ReviewService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@AllArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<ReviewResponseDTO>> createReview(@Valid @RequestBody ReviewRequestDTO requestDTO, Authentication authentication) {
        ReviewResponseDTO responseDTO = reviewService.createReview(requestDTO, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.CREATED, "Successfully submitted review", responseDTO);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<ReviewResponseDTO>>> getAllReviews(Authentication authentication, Pageable pageable) {
        Page<ReviewResponseDTO> responseDTOPage = reviewService.getAllReviews(authentication, pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all reviews", responseDTOPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ReviewResponseDTO>> getReviewById(@PathVariable UUID id) {
        ReviewResponseDTO responseDTO = reviewService.getReviewById(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched review", responseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<ReviewResponseDTO>> updateReview(@PathVariable UUID id, @Valid @RequestBody ReviewRequestDTO requestDTO, Authentication authentication) {
        ReviewResponseDTO responseDTO = reviewService.updateReview(id, requestDTO, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully updated review", responseDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<Void>> deleteReview(@PathVariable UUID id, Authentication authentication) {
        reviewService.deleteReview(id, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully deleted review", null);
    }
}
