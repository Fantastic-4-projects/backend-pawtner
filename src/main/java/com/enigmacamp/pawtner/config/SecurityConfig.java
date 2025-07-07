package com.enigmacamp.pawtner.config;

import com.enigmacamp.pawtner.constant.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint; // Inject the custom entry point

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:*"); // For local development
        configuration.addAllowedOriginPattern("http://10.10.102.128:*"); // For Expo Go on local network
        configuration.addAllowedOriginPattern("https://*.ngrok-free.app"); // For Ngrok free tier
        configuration.addAllowedOriginPattern("https://*.ngrok.io"); // For Ngrok older domains
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
                        // Public endpoints
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/code/**", "/api/payments/**", "/api/ai/**").permitAll()
                        .requestMatchers(
                                "/api/products", // GET all products
                                "/api/products/{id}", // GET product by ID
                                "/api/services", // GET all services
                                "/api/services/{id}", // GET service by ID
                                "/api/business", // GET all businesses
                                "/api/business/{id}", // GET business by ID
                                "/api/users/{id}" // GET user by ID (public for now, adjust if needed)
                        ).permitAll() // Publicly accessible GET endpoints

                        // Endpoints requiring authentication (any role)
                        .requestMatchers(
                                "/api/reviews", // GET all reviews
                                "/api/reviews/{id}", // GET review by ID
                                "/api/users" // PUT update user
                        ).authenticated()

                        // Endpoints for BUSINESS_OWNER
                        .requestMatchers(
                                "/api/products", // POST create product
                                "/api/products/{id}", // PUT update product, DELETE delete product
                                "/api/services", // POST create service
                                "/api/services/{id}", // PUT update service, DELETE delete service
                                "/api/business/register", // POST register business
                                "/api/business/my-business", // GET my business
                                "/api/orders/business"
                        ).hasAuthority(UserRole.BUSINESS_OWNER.name())

                        // Endpoints for CUSTOMER
                        .requestMatchers(
                                "/api/cart/**",
                                "/api/orders/checkout",
                                "/api/orders",
                                "/api/pets/**",
                                "/api/reviews"
                        ).hasAuthority(UserRole.CUSTOMER.name())

                        // Endpoints for ADMIN
                        .requestMatchers(
                                "/api/users",
                                "/api/users/{id}",
                                "/api/users/{id}/status",
                                "/api/auth/user/set-role",
                                "/api/reviews/{id}",
                                "/api/business/{id}"
                        ).hasAuthority(UserRole.ADMIN.name())

                        .requestMatchers("/api/orders/{order_id}"
                        ).hasAnyAuthority(UserRole.CUSTOMER.name(), UserRole.BUSINESS_OWNER.name(), UserRole.ADMIN.name())

                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 ->
                        oauth2.successHandler(oAuth2SuccessHandler)
                )
                .build();
    }
}
