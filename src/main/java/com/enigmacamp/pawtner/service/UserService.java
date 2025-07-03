package com.enigmacamp.pawtner.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.enigmacamp.pawtner.entity.User;

import com.enigmacamp.pawtner.dto.request.UserRequestDTO;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    User getUserByEmailForInternal(String email);
    UserResponseDTO getUserById(String id);
    UserResponseDTO updateUser(UserRequestDTO userRequestDTO);
    List<UserResponseDTO> getAllUser();
    UserResponseDTO updateUserStatus(UUID id, String action, Boolean value);
    void deleteUser(String id);
}
