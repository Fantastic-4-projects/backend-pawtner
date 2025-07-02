package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.UserRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.UserResponseDTO;
import com.enigmacamp.pawtner.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        UserResponseDTO userResponseDTO = userService.getUserById(id);
        CommonResponse<UserResponseDTO> response = CommonResponse.<UserResponseDTO>builder()
                .message("Successfully get user by id")
                .data(userResponseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO userResponseDTO = userService.updateUser(userRequestDTO);
        CommonResponse<UserResponseDTO> response = CommonResponse.<UserResponseDTO>builder()
                .message("Successfully update user")
                .data(userResponseDTO)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUser() {
        List<UserResponseDTO> userResponseDTOS = userService.getAllUser();
        CommonResponse<List<UserResponseDTO>> response = CommonResponse.<List<UserResponseDTO>>builder()
                .message("Successfully get all users")
                .data(userResponseDTOS)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        CommonResponse<String> response = CommonResponse.<String>builder()
                .message("Successfully delete user")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
