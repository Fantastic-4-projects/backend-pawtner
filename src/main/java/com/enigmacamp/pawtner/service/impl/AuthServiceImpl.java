package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.*;
import com.enigmacamp.pawtner.dto.response.LoginResponseDTO;
import com.enigmacamp.pawtner.dto.response.RegisterResponseDTO;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.AuthRepository;
import com.enigmacamp.pawtner.service.AuthService;
import com.enigmacamp.pawtner.config.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailServiceImpl emailService;

    public RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO) {
        User user = User.builder()
                .email(registerRequestDTO.getEmail())
                .phoneNumber(registerRequestDTO.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(registerRequestDTO.getPassword()))
                .name(registerRequestDTO.getName())
                .address(registerRequestDTO.getAddress())
                .role(UserRole.CUSTOMER)
                .codeExpire(LocalDateTime.now().plusMinutes(3))
                .codeVerification(generateRandomCode(6))
                .build();
        authRepository.save(user);

        emailService.sendVerificationCodeEmail(registerRequestDTO.getEmail(), user.getName(), user.getCodeVerification());

        return RegisterResponseDTO.builder()
                .email(user.getEmail())
                .build();
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getEmail(),
                        loginRequestDTO.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();

        if (!user.getIsVerified()){
            throw new BadCredentialsException("Email belum terverifikasi.");
        }

        String token = jwtService.generateToken(user);

        return LoginResponseDTO.builder()
                .email(user.getEmail())
                .token(token)
                .build();
    }

    @Override
    public void verify(VerificationRequestDTO requestDTO) {
        User user = authRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Pengguna dengan email " + requestDTO.getEmail() + " tidak ditemukan."));

        if (user.getIsVerified()) {
            throw new RuntimeException("Akun sudah diverifikasi.");
        }

        if (user.getCodeVerification().equals(requestDTO.getVerificationCode())) {
            if (user.getCodeExpire().isAfter(LocalDateTime.now())) {
                user.setIsVerified(true);
                user.setCodeVerification(null);
                user.setCodeExpire(null);
                authRepository.save(user);
            } else {
                throw new RuntimeException("Kode verifikasi sudah kadaluarsa. Harap memina kode baru.");
            }
        } else {
            throw new BadCredentialsException("Kode verifikasi salah.");
        }
    }

    @Override
    public void resendVerificationCode(ResendVerificationRequestDTO verificationRequestDTO) {
        User user = authRepository.findByEmail(verificationRequestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getIsVerified()) {
            throw new RuntimeException("Account already verified");
        }

        String code = generateRandomCode(6);
        user.setCodeVerification(code);
        user.setCodeExpire(LocalDateTime.now().plusMinutes(3));
        authRepository.save(user);

        emailService.sendVerificationCodeEmail(user.getEmail(), user.getName(), code);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO) {
        User user = authRepository.findByEmail(forgotPasswordRequestDTO.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email tidak ditemukan"));

        String token = UUID.randomUUID().toString();

        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpire(LocalDateTime.now().plusHours(1));
        authRepository.save(user);

        // Link ini menyesuaikan nanti sama FE
        String resetLink = "http://example.com?token=" + token;

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO) {
        User user = authRepository.findByResetPasswordToken(resetPasswordRequestDTO.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token tidak ditemukan"));

        if (user.getResetPasswordTokenExpire().isBefore(LocalDateTime.now())) {
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpire(null);
            authRepository.save(user);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token has expired.");
        }

        user.setPasswordHash(passwordEncoder.encode(resetPasswordRequestDTO.getNewPassword()));

        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpire(null);

        authRepository.save(user);
    }

    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
