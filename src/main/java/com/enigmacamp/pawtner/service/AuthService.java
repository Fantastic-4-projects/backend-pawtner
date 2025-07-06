package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.*;
import com.enigmacamp.pawtner.dto.response.LoginResponseDTO;
import com.enigmacamp.pawtner.dto.response.RegisterResponseDTO;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
    RegisterResponseDTO register(RegisterRequestDTO registerRequestDTO, UserRole userRole);
    UserRole setRoleUser(RegisterRequestDTO registerRequestDTO);
    void verify(VerificationRequestDTO requestDTO);
    void resendVerificationCode(ResendVerificationRequestDTO verificationRequestDTO);
    void forgotPassword(ForgotPasswordRequestDTO forgotPasswordRequestDTO);
    void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO);
}
