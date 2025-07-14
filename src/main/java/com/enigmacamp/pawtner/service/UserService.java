package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.ChangePasswordRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateUserStatusRequestDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.enigmacamp.pawtner.entity.User;

import com.enigmacamp.pawtner.dto.request.UserRequestDTO;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface UserService extends UserDetailsService {
    User getUserByEmailForInternal(String email);
    UserResponseDTO getUserById(String id);
    UserResponseDTO updateUser(UserRequestDTO userRequestDTO, MultipartFile profileImage);
    List<UserResponseDTO> getAllUser(Authentication authentication);
    List<User> getAllUsers();
    UserResponseDTO updateUserStatus(UUID id, UpdateUserStatusRequestDTO requestDTO);
    void deleteUser(String id);
    void changePassword(ChangePasswordRequestDTO requestDTO, Authentication authentication);
}
