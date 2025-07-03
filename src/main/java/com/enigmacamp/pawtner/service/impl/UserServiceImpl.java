package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.UserRequestDTO;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.AuthRepository;
import com.enigmacamp.pawtner.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public User getUserByEmailForInternal(String email) {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public UserResponseDTO getUserById(String id) {
        User user = authRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return mapToResponse(user);
    }

    @Override
    public UserResponseDTO updateUser(UserRequestDTO userRequestDTO) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authRepository.findByEmail(userDetails.getUsername()).get();

        user.setName(userRequestDTO.getName());
        user.setAddress(userRequestDTO.getAddress());
        user.setPhoneNumber(userRequestDTO.getPhone());
        authRepository.save(user);

        return mapToResponse(user);
    }

    @Override
    public List<UserResponseDTO> getAllUser() {
        List<User> users = authRepository.findAll();
        return users.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO updateUserStatus(UUID id, String action, Boolean value) {
        User user = authRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        switch (action.toLowerCase()) {
            case "ban" -> user.setIsEnabled(!value);
            case "suspend" -> user.setIsAccountNonLocked(!value);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid action");
        }

        authRepository.save(user);
        return mapToResponse(user);
    }


    @Override
    public void deleteUser(String id) {
        User user = authRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        authRepository.delete(user);
    }

    private UserResponseDTO mapToResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .address(user.getAddress())
                .phone(user.getPhoneNumber())
                .build();
    }
}