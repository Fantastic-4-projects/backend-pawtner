package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.ChangePasswordRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateUserStatusRequestDTO;
import com.enigmacamp.pawtner.dto.request.UserRequestDTO;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.service.EmailService;
import com.enigmacamp.pawtner.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageUploadService imageUploadService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .email("john.doe@example.com")
                .passwordHash("oldHashedPassword")
                .build();
    }

    @Test
    @DisplayName("loadUserByUsername should return UserDetails when user found")
    void loadUserByUsername_shouldReturnUserDetails_whenFound() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userService.loadUserByUsername("john.doe@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("loadUserByUsername should throw UsernameNotFoundException when user not found")
    void loadUserByUsername_shouldThrowException_whenNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("getUserById should return UserResponseDTO when user found")
    void getUserById_shouldReturnDTO_whenFound() {
        // Given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        UserResponseDTO result = userService.getUserById(user.getId().toString());

        // Then
        assertThat(result.getId()).isEqualTo(user.getId().toString());
        assertThat(result.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("updateUser should update user details and image")
    void updateUser_shouldUpdateDetailsAndImage() throws IOException {
        // Given
        UserRequestDTO requestDTO = UserRequestDTO.builder().name("John Smith").address("New Address").phone("123").build();
        MockMultipartFile profileImage = new MockMultipartFile("image", "profile.jpg", "image/jpeg", "data".getBytes());
        String newImageUrl = "http://image.url/profile.jpg";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(imageUploadService.upload(profileImage)).thenReturn(newImageUrl);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponseDTO result = userService.updateUser(requestDTO, profileImage);

        // Then
        assertThat(result.getName()).isEqualTo("John Smith");
        assertThat(result.getAddress()).isEqualTo("New Address");
        assertThat(result.getPhone()).isEqualTo("123");
        assertThat(result.getImageUrl()).isEqualTo(newImageUrl);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserStatus should ban user and send email")
    void updateUserStatus_shouldBanUserAndSendEmail() {
        // Given
        UpdateUserStatusRequestDTO requestDTO = new UpdateUserStatusRequestDTO("ban", false, true, "Violation of terms");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When
        userService.updateUserStatus(user.getId(), requestDTO);

        // Then
        assertThat(user.getIsEnabled()).isFalse();
        verify(emailService).sendUserStatusChangeEmail(user.getEmail(), user.getName(), "ban", false, "Violation of terms");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("changePassword should update password when old password is correct")
    void changePassword_shouldSucceed_whenOldPasswordMatches() {
        // Given
        ChangePasswordRequestDTO requestDTO = new ChangePasswordRequestDTO("oldPassword", "newPassword");
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");

        // When
        userService.changePassword(requestDTO, authentication);

        // Then
        verify(userRepository).save(any(User.class));
        assertThat(user.getPasswordHash()).isEqualTo("newHashedPassword");
    }

    @Test
    @DisplayName("changePassword should throw exception when old password is incorrect")
    void changePassword_shouldFail_whenOldPasswordDoesNotMatch() {
        // Given
        ChangePasswordRequestDTO requestDTO = new ChangePasswordRequestDTO("wrongOldPassword", "newPassword");
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", "oldHashedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.changePassword(requestDTO, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Password lama yang Anda masukkan salah.");
        verify(userRepository, never()).save(any(User.class));
    }
}