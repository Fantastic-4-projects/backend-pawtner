package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.dto.request.ChangePasswordRequestDTO;
import com.enigmacamp.pawtner.dto.request.UpdateUserStatusRequestDTO;
import com.enigmacamp.pawtner.dto.request.UserRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to create a mock Authentication object
    private Authentication createMockAuthentication(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                new User(username, "", Collections.singletonList(new SimpleGrantedAuthority(role))),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("GET /api/users/{id} should return user by ID and status 200")
    void getUserById_shouldReturnUser_whenIdExists() throws Exception {
        String userId = UUID.randomUUID().toString();
        UserResponseDTO mockResponse = UserResponseDTO.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .build();

        when(userService.getUserById(userId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully get user by id"))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.name").value("Test User"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 404 when user not found")
    void getUserById_shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
        String userId = UUID.randomUUID().toString();

        when(userService.getUserById(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("PUT /api/users should update user and return status 200")
    void updateUser_shouldUpdateUser_whenValidDataProvided() throws Exception {
        UserRequestDTO requestDTO = UserRequestDTO.builder()
                .name("Updated Name")
                .phone("1234567890")
                .build();
        UserResponseDTO mockResponse = UserResponseDTO.builder()
                .id(UUID.randomUUID().toString())
                .name("Updated Name")
                .email("test@example.com")
                .phone("1234567890")
                .build();

        MockMultipartFile userPart = new MockMultipartFile("user", "", "application/json", objectMapper.writeValueAsBytes(requestDTO));
        MockMultipartFile profileImagePart = new MockMultipartFile("profileImage", "profile.jpg", "image/jpeg", "some-image-bytes".getBytes());

        when(userService.updateUser(any(UserRequestDTO.class), any())).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/users")
                        .file(userPart)
                        .file(profileImagePart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully update user"))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));

        verify(userService, times(1)).updateUser(any(UserRequestDTO.class), any());
    }

    @Test
    @DisplayName("GET /api/users should return all users for ADMIN and status 200")
    void getAllUser_shouldReturnAllUsers_whenAdmin() throws Exception {
        List<UserResponseDTO> mockResponse = Collections.singletonList(
                UserResponseDTO.builder().id(UUID.randomUUID().toString()).name("Admin User").build()
        );

        Authentication mockAuthentication = createMockAuthentication("admin@example.com", "ADMIN");

        when(userService.getAllUser(any(Authentication.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/users")
                        .principal(mockAuthentication)) // Pass the mock Authentication object
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully get all users"))
                .andExpect(jsonPath("$.data[0].name").value("Admin User"));

        verify(userService, times(1)).getAllUser(any(Authentication.class));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} should delete user and return status 200")
    void deleteUser_shouldDeleteUser_whenAdmin() throws Exception {
        String userId = UUID.randomUUID().toString();

        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully delete user"));

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/status should update user status and return status 200")
    void updateStatus_shouldUpdateUserStatus_whenAdmin() throws Exception {
        UUID userId = UUID.randomUUID();
        UpdateUserStatusRequestDTO requestDTO = new UpdateUserStatusRequestDTO("ban", true, true, "reason");
        UserResponseDTO mockResponse = UserResponseDTO.builder()
                .id(userId.toString())
                .name("Test User")
                .isEnable(false)
                .isNoLocked(false)
                .build();

        when(userService.updateUserStatus(eq(userId), any(UpdateUserStatusRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/users/{id}/status", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Status pengguna berhasil diperbarui. Notifikasi telah dikirimkan."))
                .andExpect(jsonPath("$.data.isEnable").value(false));

        verify(userService, times(1)).updateUserStatus(eq(userId), any(UpdateUserStatusRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/users/change-password should change password and return status 200")
    void changePassword_shouldChangePassword_whenAuthenticated() throws Exception {
        ChangePasswordRequestDTO requestDTO = ChangePasswordRequestDTO.builder()
                .oldPassword("oldPass")
                .newPassword("newPass")
                .build();

        Authentication mockAuthentication = createMockAuthentication("user@example.com", "CUSTOMER");

        doNothing().when(userService).changePassword(any(ChangePasswordRequestDTO.class), any(Authentication.class));

        mockMvc.perform(patch("/api/users/change-password")
                        .principal(mockAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Password berhasil diubah."));

        verify(userService, times(1)).changePassword(any(ChangePasswordRequestDTO.class), any(Authentication.class));
    }
}