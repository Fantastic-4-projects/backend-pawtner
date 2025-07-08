package com.enigmacamp.pawtner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponseDTO {
    private UUID id;
    private UserResponseDTO user;
    private BusinessResponseDTO business;
    private UUID productId;
    private UUID serviceId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
