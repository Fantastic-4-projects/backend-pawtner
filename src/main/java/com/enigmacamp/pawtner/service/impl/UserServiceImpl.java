package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.ChangePasswordRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateUserStatusRequestDTO;
import com.enigmacamp.pawtner.dto.request.UserRequestDTO;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.mapper.UserMapper;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.service.EmailService;
import com.enigmacamp.pawtner.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.enigmacamp.pawtner.service.ImageUploadService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public User getUserByEmailForInternal(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    

    @Override
    public UserResponseDTO getUserById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserMapper.mapToResponse(user);
    }

    @Override
    public UserResponseDTO updateUser(UserRequestDTO userRequestDTO, MultipartFile profileImage) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).get();

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String imageUrl = imageUploadService.upload(profileImage);
                user.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
            }
        }

        user.setName(userRequestDTO.getName());
        user.setAddress(userRequestDTO.getAddress());
        user.setPhoneNumber(userRequestDTO.getPhone());
        userRepository.save(user);

        return UserMapper.mapToResponse(user);
    }

    @Override
    public List<UserResponseDTO> getAllUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if (user.getRole().name().equals("ADMIN")) {
            List<User> users = userRepository.findAll();
            return users.stream().map(UserMapper::mapToResponse).collect(Collectors.toList());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserResponseDTO updateUserStatus(UUID id, UpdateUserStatusRequestDTO updateUserStatus) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String action = updateUserStatus.getAction().toLowerCase();
        Boolean value = updateUserStatus.getValue();

        switch (action) {
            case "ban" -> user.setIsEnabled(value);
            case "suspend" -> user.setIsAccountNonLocked(value);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aksi tidak valid: " + updateUserStatus.getAction());
        }

        userRepository.save(user);

        if (updateUserStatus.getIsSend()) {
            emailService.sendUserStatusChangeEmail(
                    user.getEmail(),
                    user.getName(),
                    action,
                    value,
                    updateUserStatus.getReason()
            );
        }

        return UserMapper.mapToResponse(user);
    }


    @Override
    public void deleteUser(String id) {
        User user = userRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDTO requestDTO, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan."));

        if (!passwordEncoder.matches(requestDTO.getOldPassword(), currentUser.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password lama yang Anda masukkan salah.");
        }

        currentUser.setPasswordHash(passwordEncoder.encode(requestDTO.getNewPassword()));

        userRepository.save(currentUser);

        log.info("Password untuk pengguna {} telah berhasil diubah.", currentUser.getEmail());
    }
}