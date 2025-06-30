package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.LoginRequestDTO;
import com.enigmacamp.pawtner.dto.request.RegisterRequestDTO;
import com.enigmacamp.pawtner.dto.request.ResendVerificationRequestDTO;
import com.enigmacamp.pawtner.dto.request.VerificationRequestDTO;
import com.enigmacamp.pawtner.dto.response.LoginResponseDTO;
import com.enigmacamp.pawtner.dto.response.RegisterResponseDTO;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.AuthRepository;
import com.enigmacamp.pawtner.service.AuthService;
import com.enigmacamp.pawtner.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

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
        User user = (User) authRepository.findByEmail(requestDTO.getEmail())
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
        User user = (User) authRepository.findByEmail(verificationRequestDTO.getEmail())
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

    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
