package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.LoginRequestDTO;
import com.enigmacamp.pawtner.dto.request.ResendVerificationRequestDTO;
import com.enigmacamp.pawtner.dto.request.VerificationRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.LoginResponseDTO;
import com.enigmacamp.pawtner.dto.request.RegisterRequestDTO;
import com.enigmacamp.pawtner.dto.response.RegisterResponseDTO;
import com.enigmacamp.pawtner.service.AuthService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<RegisterResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {

        return ResponseUtil.createResponse(
                HttpStatus.CREATED,
                "Selamat datang di Pawtner. Kode verifikasi telah dikirim ke email Anda.",
                authService.register(registerRequestDTO)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Anda berhasil login.",
                authService.login(loginRequestDTO)
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<CommonResponse<String>> verify(@Valid @RequestBody VerificationRequestDTO request) {
        authService.verify(request);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Akun berhasil diverifikasi.",
                null
        );
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<CommonResponse<String>> resendVerification(@Valid @RequestBody ResendVerificationRequestDTO request) {
        authService.resendVerificationCode(request);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Kode verifikasi telah dikirim ke email Anda.",
                null
        );
    }
}
