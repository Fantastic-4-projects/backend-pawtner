package com.enigmacamp.pawtner.config;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.AuthRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final AuthRepository authRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        log.info("Successfully authenticated user via Google with email: {}", email);

        User user = authRepository.findByEmail(email).orElseGet(() -> {
            log.info("User with email {} not found. Creating a new user.", email);
            User newUser = User.builder()
                    .email(email)
                    .name(oauth2User.getAttribute("name"))
                    .imageUrl(oauth2User.getAttribute("picture"))
                    .providerId(oauth2User.getAttribute("sub"))
                    .role(UserRole.CUSTOMER)
                    .isVerified(true)
                    .authProvider("google")
                    .build();

            log.info(oauth2User.toString());
            return authRepository.save(newUser);
        });

        String token = jwtService.generateToken(user);
        log.info("Generated Pawtner JWT for user {}", email);

//        Menyesuaikan dengan redirect FE mas eris
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-redirect")
                .queryParam("token", token)
                .build().toUriString();

        clearAuthenticationAttributes(request);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}