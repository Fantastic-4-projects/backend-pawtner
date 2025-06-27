package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.LoginRequestDTO;
import com.enigmacamp.pawtner.dto.request.ResendVerificationRequestDTO;
import com.enigmacamp.pawtner.dto.request.VerificationRequestDTO;
import com.enigmacamp.pawtner.dto.response.LoginResponseDTO;
import com.enigmacamp.pawtner.dto.request.RegisterRequestDTO;
import com.enigmacamp.pawtner.dto.response.RegisterResponseDTO;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
    RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO);
    void verify(VerificationRequestDTO requestDTO);
    void resendVerificationCode(ResendVerificationRequestDTO verificationRequestDTO);
}
