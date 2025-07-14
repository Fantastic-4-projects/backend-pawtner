package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.ForgotPasswordRequestDTO;
import com.enigmacamp.pawtner.dto.request.LoginRequestDTO;
import com.enigmacamp.pawtner.dto.request.RegisterRequestDTO;
import com.enigmacamp.pawtner.dto.request.ResendVerificationRequestDTO;
import com.enigmacamp.pawtner.dto.request.ResetPasswordRequestDTO;
import com.enigmacamp.pawtner.dto.request.VerificationRequestDTO;
import com.enigmacamp.pawtner.dto.response.LoginResponseDTO;
import com.enigmacamp.pawtner.dto.response.RegisterResponseDTO;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailServiceImpl emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDTO registerRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private VerificationRequestDTO verificationRequestDTO;
    private ResendVerificationRequestDTO resendVerificationRequestDTO;
    private ForgotPasswordRequestDTO forgotPasswordRequestDTO;
    private ResetPasswordRequestDTO resetPasswordRequestDTO;

    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequestDTO = RegisterRequestDTO.builder()
                .email("test.user@example.com")
                .password("password123")
                .name("Test User")
                .address("Test Address")
                .phoneNumber("08123456789")
                .build();

        loginRequestDTO = LoginRequestDTO.builder()
                .email("test.user@example.com")
                .password("password123")
                .build();

        verificationRequestDTO = VerificationRequestDTO.builder()
                .email("test.user@example.com")
                .verificationCode("123456")
                .build();

        resendVerificationRequestDTO = ResendVerificationRequestDTO.builder()
                .email("test.user@example.com")
                .build();

        forgotPasswordRequestDTO = ForgotPasswordRequestDTO.builder()
                .email("test.user@example.com")
                .build();

        resetPasswordRequestDTO = ResetPasswordRequestDTO.builder()
                .token(UUID.randomUUID().toString())
                .newPassword("newPassword123")
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test.user@example.com")
                .name("Test User")
                .passwordHash("hashedPassword")
                .role(UserRole.CUSTOMER)
                .isVerified(false)
                .codeVerification("123456")
                .codeExpire(LocalDateTime.now().plusMinutes(3))
                .isEnabled(true)
                .isCredentialsNonExpired(true)
                .isAccountNonLocked(true)
                .isAccountNonExpired(true)
                .build();

        // Set the resetPasswordUrl using ReflectionTestUtils
        ReflectionTestUtils.setField(authService, "resetPasswordUrl", "http://localhost:8080/reset-password");
    }

    // =================================== REGISTER TESTS ===================================
    @Test
    @DisplayName("should save user and send verification email when email is new")
    void register_shouldSaveUserAndSendEmail_whenEmailIsNew() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendVerificationCodeEmail(anyString(), anyString(), anyString());

        RegisterResponseDTO response = authService.register(registerRequestDTO, UserRole.CUSTOMER);

        verify(userRepository, times(1)).existsByEmail("test.user@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(emailService, times(1)).sendVerificationCodeEmail(eq("test.user@example.com"), eq("Test User"), anyString());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getEmail()).isEqualTo("test.user@example.com");
        assertThat(capturedUser.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(response.getEmail()).isEqualTo("test.user@example.com");
    }

    @Test
    @DisplayName("should throw ResponseStatusException when email already exists during registration")
    void register_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequestDTO, UserRole.CUSTOMER))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationCodeEmail(anyString(), anyString(), anyString());
    }

    // =================================== SET ROLE USER TESTS ===================================
    @Test
    @DisplayName("should set user role successfully")
    void setRoleUser_shouldSucceed() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .email("test.user@example.com")
                .role(UserRole.BUSINESS_OWNER)
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserRole result = authService.setRoleUser(request);

        verify(userRepository, times(1)).findByEmail("test.user@example.com");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.BUSINESS_OWNER);
        assertThat(result).isEqualTo(UserRole.BUSINESS_OWNER);
    }

    @Test
    @DisplayName("should throw ResponseStatusException when user not found for setRoleUser")
    void setRoleUser_shouldThrowException_whenUserNotFound() {
        RegisterRequestDTO request = RegisterRequestDTO.builder()
                .email("nonexistent@example.com")
                .role(UserRole.BUSINESS_OWNER)
                .build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.setRoleUser(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email tidak ditemukan");

        verify(userRepository, never()).save(any(User.class));
    }

    // =================================== LOGIN TESTS ===================================
    @Test
    @DisplayName("should return LoginResponseDTO on successful login")
    void login_shouldReturnLoginResponseDTO_onSuccess() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser.toBuilder().isVerified(true).build()); // Ensure user is verified
        when(jwtService.generateToken(any(User.class))).thenReturn("mockedToken");

        LoginResponseDTO response = authService.login(loginRequestDTO);

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(any(User.class));
        assertThat(response.getEmail()).isEqualTo("test.user@example.com");
        assertThat(response.getToken()).isEqualTo("mockedToken");
    }

    @Test
    @DisplayName("should throw BadCredentialsException when user is not verified during login")
    void login_shouldThrowBadCredentialsException_whenUserNotVerified() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser); // User is not verified

        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Email belum terverifikasi.");

        verify(jwtService, never()).generateToken(any(User.class));
    }

    // =================================== VERIFY TESTS ===================================
    @Test
    @DisplayName("should verify user successfully with correct code and not expired")
    void verify_shouldSucceed_whenCodeCorrectAndNotExpired() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.verify(verificationRequestDTO);

        verify(userRepository, times(1)).findByEmail("test.user@example.com");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getIsVerified()).isTrue();
        assertThat(userCaptor.getValue().getCodeVerification()).isNull();
        assertThat(userCaptor.getValue().getCodeExpire()).isNull();
    }

    @Test
    @DisplayName("should throw RuntimeException when user not found for verification")
    void verify_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verify(verificationRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pengguna dengan email test.user@example.com tidak ditemukan.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should throw RuntimeException when account is already verified")
    void verify_shouldThrowException_whenAccountAlreadyVerified() {
        testUser.setIsVerified(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.verify(verificationRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Akun sudah diverifikasi.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should throw BadCredentialsException when verification code is incorrect")
    void verify_shouldThrowBadCredentialsException_whenCodeIncorrect() {
        verificationRequestDTO.setVerificationCode("wrongCode");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.verify(verificationRequestDTO))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Kode verifikasi salah.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should throw RuntimeException when verification code is expired")
    void verify_shouldThrowException_whenCodeExpired() {
        testUser.setCodeExpire(LocalDateTime.now().minusMinutes(1)); // Set code as expired
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.verify(verificationRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kode verifikasi sudah kadaluarsa. Harap memina kode baru.");

        
    }

    // =================================== RESEND VERIFICATION CODE TESTS ===================================
    @Test
    @DisplayName("should resend verification code successfully")
    void resendVerificationCode_shouldSucceed() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendVerificationCodeEmail(anyString(), anyString(), anyString());

        authService.resendVerificationCode(resendVerificationRequestDTO);

        verify(userRepository, times(1)).findByEmail("test.user@example.com");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getCodeVerification()).isNotNull();
        assertThat(userCaptor.getValue().getCodeExpire()).isAfter(LocalDateTime.now());
        verify(emailService, times(1)).sendVerificationCodeEmail(eq("test.user@example.com"), eq("Test User"), anyString());
    }

    @Test
    @DisplayName("should throw RuntimeException when user not found for resend verification")
    void resendVerificationCode_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resendVerificationCode(resendVerificationRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationCodeEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("should throw ResponseStatusException when account is already verified for resend verification")
    void resendVerificationCode_shouldThrowException_whenAccountAlreadyVerified() {
        testUser.setIsVerified(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.resendVerificationCode(resendVerificationRequestDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account already verified");

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationCodeEmail(anyString(), anyString(), anyString());
    }

    // =================================== FORGOT PASSWORD TESTS ===================================
    @Test
    @DisplayName("should send password reset email successfully")
    void forgotPassword_shouldSendEmail_onSuccess() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        authService.forgotPassword(forgotPasswordRequestDTO);

        verify(userRepository, times(1)).findByEmail("test.user@example.com");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getResetPasswordToken()).isNotNull();
        assertThat(userCaptor.getValue().getResetPasswordTokenExpire()).isAfter(LocalDateTime.now());
        verify(emailService, times(1)).sendPasswordResetEmail(eq("test.user@example.com"), eq("Test User"), contains("http://localhost:8080/reset-password?token="));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when user not found for forgot password")
    void forgotPassword_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword(forgotPasswordRequestDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email tidak ditemukan");

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    // =================================== RESET PASSWORD TESTS ===================================
    @Test
    @DisplayName("should reset password successfully with valid token")
    void resetPassword_shouldSucceed_whenTokenValid() {
        testUser.setResetPasswordToken(resetPasswordRequestDTO.getToken());
        testUser.setResetPasswordTokenExpire(LocalDateTime.now().plusHours(1));
        when(userRepository.findByResetPasswordToken(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.resetPassword(resetPasswordRequestDTO);

        verify(userRepository, times(1)).findByResetPasswordToken(resetPasswordRequestDTO.getToken());
        verify(passwordEncoder, times(1)).encode("newPassword123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("newHashedPassword");
        assertThat(userCaptor.getValue().getResetPasswordToken()).isNull();
        assertThat(userCaptor.getValue().getResetPasswordTokenExpire()).isNull();
    }

    @Test
    @DisplayName("should throw ResponseStatusException when token not found for reset password")
    void resetPassword_shouldThrowException_whenTokenNotFound() {
        when(userRepository.findByResetPasswordToken(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(resetPasswordRequestDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token tidak ditemukan");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when token is expired for reset password")
    void resetPassword_shouldThrowException_whenTokenExpired() {
        testUser.setResetPasswordToken(resetPasswordRequestDTO.getToken());
        testUser.setResetPasswordTokenExpire(LocalDateTime.now().minusHours(1)); // Set token as expired
        when(userRepository.findByResetPasswordToken(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser); // Should save to clear token

        assertThatThrownBy(() -> authService.resetPassword(resetPasswordRequestDTO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Password reset token has expired.");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }
}