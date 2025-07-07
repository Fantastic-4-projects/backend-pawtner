package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.ChangePasswordRequestDTO;
import com.enigmacamp.pawtner.dto.request.UserRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
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
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<UserResponseDTO>> getUserById(@PathVariable String id) {
        UserResponseDTO userResponseDTO = userService.getUserById(id);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Successfully get user by id",
                userResponseDTO
        );
    }

    @PutMapping(consumes = "multipart/form-data")
    public ResponseEntity<CommonResponse<UserResponseDTO>> updateUser(
            @RequestPart(name = "user", required = false) UserRequestDTO userRequestDTO,
            @RequestPart(name = "profileImage", required = false) MultipartFile profileImage
    ) {
        UserResponseDTO userResponseDTO = userService.updateUser(userRequestDTO, profileImage);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Successfully update user",
                userResponseDTO
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<List<UserResponseDTO>>> getAllUser(Authentication authentication) {
        List<UserResponseDTO> userResponseDTOS = userService.getAllUser(authentication);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Successfully get all users",
                userResponseDTOS

        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<String>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Successfully delete user",
                null
        );
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

    @PatchMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO requestDTO,
            Authentication authentication
    ) {
        userService.changePassword(requestDTO, authentication);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Password berhasil diubah.",
                null
        );
    }
}
