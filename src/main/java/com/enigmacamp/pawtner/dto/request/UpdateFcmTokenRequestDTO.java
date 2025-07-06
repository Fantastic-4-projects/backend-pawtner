package com.enigmacamp.pawtner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFcmTokenRequestDTO {
    @NotBlank(message = "FCM token is required")
    private String fcmToken;
}
