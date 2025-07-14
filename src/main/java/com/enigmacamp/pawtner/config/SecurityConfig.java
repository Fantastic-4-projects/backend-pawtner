package com.enigmacamp.pawtner.config;

import com.enigmacamp.pawtner.constant.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthTokenFilter authTokenFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:*");
        configuration.addAllowedOriginPattern("https://pawtner-id.web.app");
        configuration.addAllowedOriginPattern("http://10.10.102.128:*");
        configuration.addAllowedOriginPattern("https://*.ngrok-free.app");
        configuration.addAllowedOriginPattern("https://*.ngrok.io");
        configuration.addAllowedOriginPattern("http://10.10.102.68:5173/");
        configuration.addAllowedOrigin("https://measured-lively-hippo.ngrok-free.app");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint)) // Use custom entry point
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/code/**", "/api/payments/**", "/api/ai/**").permitAll()
                        .requestMatchers(
                                "/api/products",
                                "/api/products/{id}",
                                "/api/services",
                                "/api/services/{id}",
                                "/api/business",
                                "/api/business/{id}"
                        ).permitAll()

                        .requestMatchers(
                                "/api/reviews",
                                "/api/reviews/{id}",
                                "/api/users",
                                "/api/users/{id}",
                                "/api/users/change-password"
                        ).authenticated()

                        .requestMatchers(
                                "/api/products",
                                "/api/products/{id}",
                                "/api/services",
                                "/api/services/{id}",
                                "/api/business/register",
                                "/api/business/my-business",
                                "/api/orders/business"
                        ).hasAuthority(UserRole.BUSINESS_OWNER.name())

                        .requestMatchers(
                                "/api/cart/**",
                                "/api/orders/checkout",
                                "/api/orders",
                                "/api/orders/calculate-price",
                                "/api/pets/**",
                                "/api/reviews"
                        ).hasAuthority(UserRole.CUSTOMER.name())

                        .requestMatchers(
                                "/api/users",
                                "/api/users/{id}",
                                "/api/users/{id}/status",
                                "/api/auth/user/set-role",
                                "/api/reviews/{id}",
                                "/api/business/{id}",
                                "/api/notifications/broadcast"
                        ).hasAuthority(UserRole.ADMIN.name())

                        .requestMatchers("/api/orders/{order_id}"
                        ).hasAnyAuthority(UserRole.CUSTOMER.name(), UserRole.BUSINESS_OWNER.name(), UserRole.ADMIN.name())

                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 ->
                        oauth2.successHandler(oAuth2SuccessHandler).failureHandler(oAuth2FailureHandler)
                )
                .build();
    }
}
