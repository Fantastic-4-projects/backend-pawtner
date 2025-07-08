package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.constant.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserResponseDTO {
    private String id;
    private String email;
    private String name;
    private String address;
    private String phone;
    private String imageUrl;
    private UserRole role;
    private Boolean isEnable;
    private Boolean isNoLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
