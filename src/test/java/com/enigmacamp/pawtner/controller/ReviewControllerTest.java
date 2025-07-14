package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.dto.request.ReviewRequestDTO;
import com.enigmacamp.pawtner.dto.response.ReviewResponseDTO;
import com.enigmacamp.pawtner.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Authentication createMockAuthentication(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                new User(username, "", Collections.singletonList(new SimpleGrantedAuthority(role))),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("POST /api/reviews should create a review and return status 201")
    void createReview_shouldSucceed() throws Exception {
        ReviewRequestDTO request = ReviewRequestDTO.builder()
                .businessId(UUID.randomUUID())
                .rating(5)
                .comment("Great service!")
                .build();
        ReviewResponseDTO response = ReviewResponseDTO.builder().id(UUID.randomUUID()).rating(5).build();
        Authentication auth = createMockAuthentication("customer@example.com", "CUSTOMER");

        when(reviewService.createReview(any(ReviewRequestDTO.class), eq("customer@example.com"))).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Successfully submitted review"));
    }

    @Test
    @DisplayName("GET /api/reviews should return a page of reviews and status 200")
    void getAllReviews_shouldSucceed() throws Exception {
        Page<ReviewResponseDTO> reviewPage = new PageImpl<>(Collections.singletonList(new ReviewResponseDTO()));
        Authentication auth = createMockAuthentication("user@example.com", "CUSTOMER");

        when(reviewService.getAllReviews(eq(auth), any(Pageable.class))).thenReturn(reviewPage);

        mockMvc.perform(get("/api/reviews").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("GET /api/reviews/{id} should return a review and status 200")
    void getReviewById_shouldSucceed() throws Exception {
        UUID reviewId = UUID.randomUUID();
        ReviewResponseDTO response = ReviewResponseDTO.builder().id(reviewId).rating(4).build();

        when(reviewService.getReviewById(reviewId)).thenReturn(response);

        mockMvc.perform(get("/api/reviews/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(reviewId.toString()));
    }

    @Test
    @DisplayName("PUT /api/reviews/{id} should update a review and return status 200")
    void updateReview_shouldSucceed() throws Exception {
        UUID reviewId = UUID.randomUUID();
        ReviewRequestDTO request = ReviewRequestDTO.builder().rating(3).comment("Updated comment").build();
        ReviewResponseDTO response = ReviewResponseDTO.builder().id(reviewId).rating(3).build();
        Authentication auth = createMockAuthentication("customer@example.com", "CUSTOMER");

        when(reviewService.updateReview(eq(reviewId), any(ReviewRequestDTO.class), eq("customer@example.com"))).thenReturn(response);

        mockMvc.perform(put("/api/reviews/{id}", reviewId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully updated review"));
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} should delete a review and return status 200")
    void deleteReview_shouldSucceed() throws Exception {
        UUID reviewId = UUID.randomUUID();
        Authentication auth = createMockAuthentication("customer@example.com", "CUSTOMER");
        doNothing().when(reviewService).deleteReview(reviewId, auth.getName());

        mockMvc.perform(delete("/api/reviews/{id}", reviewId).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(reviewService, times(1)).deleteReview(reviewId, auth.getName());
    }
}