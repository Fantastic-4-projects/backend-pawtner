package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.entity.User;

public class UserMapper {
    public static UserResponseDTO mapToResponse(User user){
        return UserResponseDTO.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .address(user.getAddress())
                .phone(user.getPhoneNumber())
                .imageUrl(user.getImageUrl())
                .isEnable(user.getIsEnabled())
                .isNoLocked(user.getIsAccountNonLocked())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getCreatedAt())
                .build();
    }
}
