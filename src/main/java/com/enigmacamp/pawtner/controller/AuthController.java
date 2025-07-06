package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.*;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.LoginResponseDTO;
import com.enigmacamp.pawtner.dto.response.RegisterResponseDTO;
import com.enigmacamp.pawtner.service.AuthService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/customer")
    public ResponseEntity<CommonResponse<RegisterResponseDTO>> registerUserCustomer(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {

        return ResponseUtil.createResponse(
                HttpStatus.CREATED,
                "Selamat datang di Pawtner. Kode verifikasi telah dikirim ke email Anda.",
                authService.register(registerRequestDTO, UserRole.CUSTOMER)
        );
    }

    @PostMapping("/register/business-owner")
    public ResponseEntity<CommonResponse<RegisterResponseDTO>> registerUserBusinessOwner(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {

        return ResponseUtil.createResponse(
                HttpStatus.CREATED,
                "Selamat datang di Pawtner. Kode verifikasi telah dikirim ke email Anda.",
                authService.register(registerRequestDTO, UserRole.BUSINESS_OWNER)
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

    @PatchMapping("/user/set-role")
    public ResponseEntity<CommonResponse<UserRole>> setUserRole(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Role berhasil diubah.",
                authService.setRoleUser(registerRequestDTO)
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

    @PostMapping("/forgot-password")
    public ResponseEntity<CommonResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Jika email terdaftar, link untuk reset password telah dikirim.",
                null
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<CommonResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Password berhasil diubah.",
                null
        );
    }
}
