package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.UpdateFcmTokenRequestDTO;
import com.enigmacamp.pawtner.dto.request.UserRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.service.NotificationService;
import com.enigmacamp.pawtner.service.UserService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        UserResponseDTO userResponseDTO = userService.getUserById(id);
        CommonResponse<UserResponseDTO> response = CommonResponse.<UserResponseDTO>builder()
                .message("Successfully get user by id")
                .data(userResponseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> updateUser(
            @RequestPart(name = "user", required = false) UserRequestDTO userRequestDTO,
            @RequestPart(name = "profileImage", required = false) MultipartFile profileImage
    ) {
        UserResponseDTO userResponseDTO = userService.updateUser(userRequestDTO, profileImage);
        CommonResponse<UserResponseDTO> response = CommonResponse.<UserResponseDTO>builder()
                .message("Successfully update user")
                .data(userResponseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> getAllUser() {
        List<UserResponseDTO> userResponseDTOS = userService.getAllUser();
        CommonResponse<List<UserResponseDTO>> response = CommonResponse.<List<UserResponseDTO>>builder()
                .message("Successfully get all users")
                .data(userResponseDTOS)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        CommonResponse<String> response = CommonResponse.<String>builder()
                .message("Successfully delete user")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<UserResponseDTO>> updateStatus(
            @PathVariable UUID id,
            @RequestParam String action,
            @RequestParam Boolean value
    ) {
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Berhasil mengubah status " + action + " menjadi " + value,
                userService.updateUserStatus(id, action, value)
        );
    }
}
