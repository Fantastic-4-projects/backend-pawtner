package com.enigmacamp.pawtner.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
    @Value("${frontend.redirect-url.oauth-failure}")
    private String failureRedirectUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        log.error("OAuth2 Authentication Failed: {}", exception.getMessage());

        String reason = "oauth_error";

        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            if ("access_denied".equals(oauthEx.getError().getErrorCode())) {
                reason = "cancelled_by_user";
            }
        }

        String targetUrl = UriComponentsBuilder.fromUriString(failureRedirectUrl)
                .queryParam("status", "failure")
                .queryParam("reason", reason)
                .build().toUriString();

        response.sendRedirect(targetUrl);
    }
}
