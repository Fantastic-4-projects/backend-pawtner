package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.*;
import com.enigmacamp.pawtner.dto.response.LoginResponseDTO;
import com.enigmacamp.pawtner.dto.response.RegisterResponseDTO;
import com.enigmacamp.pawtner.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register/customer should register a customer and return status 201")
    void registerUserCustomer_shouldSucceed() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO("customer@example.com", "123", "pass", "Cust", "Addr", null);
        RegisterResponseDTO response = new RegisterResponseDTO("customer@example.com");

        when(authService.register(any(RegisterRequestDTO.class), eq(UserRole.CUSTOMER))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Selamat datang di Pawtner. Kode verifikasi telah dikirim ke email Anda."))
                .andExpect(jsonPath("$.data.email").value("customer@example.com"));

        verify(authService, times(1)).register(any(RegisterRequestDTO.class), eq(UserRole.CUSTOMER));
    }

    @Test
    @DisplayName("POST /api/auth/login should log in user and return status 200")
    void login_shouldSucceed() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "password");
        LoginResponseDTO response = LoginResponseDTO.builder().token("jwt-token").email("user@example.com").build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Anda berhasil login."))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 for bad credentials")
    void login_shouldFailWithBadCredentials() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "wrong-password");

        when(authService.login(any(LoginRequestDTO.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/verify should verify account and return status 200")
    void verify_shouldSucceed() throws Exception {
        VerificationRequestDTO request = new VerificationRequestDTO("user@example.com", "123456");
        doNothing().when(authService).verify(any(VerificationRequestDTO.class));

        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Akun berhasil diverifikasi."));

        verify(authService, times(1)).verify(any(VerificationRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/resend-verification should resend code and return status 200")
    void resendVerification_shouldSucceed() throws Exception {
        ResendVerificationRequestDTO request = new ResendVerificationRequestDTO("user@example.com");
        doNothing().when(authService).resendVerificationCode(any(ResendVerificationRequestDTO.class));

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Kode verifikasi telah dikirim ke email Anda."));

        verify(authService, times(1)).resendVerificationCode(any(ResendVerificationRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password should send reset link and return status 200")
    void forgotPassword_shouldSucceed() throws Exception {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("user@example.com");
        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequestDTO.class));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Jika email terdaftar, link untuk reset password telah dikirim."));

        verify(authService, times(1)).forgotPassword(any(ForgotPasswordRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password should reset password and return status 200")
    void resetPassword_shouldSucceed() throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("token", "newPassword");
        doNothing().when(authService).resetPassword(any(ResetPasswordRequestDTO.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Password berhasil diubah."));

        verify(authService, times(1)).resetPassword(any(ResetPasswordRequestDTO.class));
    }
}