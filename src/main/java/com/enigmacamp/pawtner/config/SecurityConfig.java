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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint)) // Use custom entry point
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/code/**", "/api/payments/**").permitAll()
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
                                "/api/reviews/{id}" // GET review by ID
                        ).authenticated()

                        // Endpoints for BUSINESS_OWNER
                        .requestMatchers(
                                "/api/products", // POST create product
                                "/api/products/{id}", // PUT update product, DELETE delete product
                                "/api/services", // POST create service
                                "/api/services/{id}", // PUT update service, DELETE delete service
                                "/api/business/register" // POST register business
                        ).hasAuthority(UserRole.BUSINESS_OWNER.name())

                        // Endpoints for CUSTOMER
                        .requestMatchers(
                                "/api/cart/**", // All cart operations
                                "/api/orders/checkout", // Checkout
                                "/api/orders", // GET my orders
                                "/api/orders/{id}", // GET order by ID (customer specific)
                                "/api/pets/**", // All pet operations
                                "/api/reviews" // POST create review, PUT update review
                        ).hasAuthority(UserRole.CUSTOMER.name())

                        // Endpoints for ADMIN
                        .requestMatchers(
                                "/api/users", // GET all users, DELETE user by ID
                                "/api/users/{id}", // DELETE user by ID, PATCH update user status
                                "/api/users/{id}/status", // PATCH update user status
                                "/api/auth/user/set-role", // PATCH set user role
                                "/api/orders/{id}", // PUT update order status, DELETE delete order
                                "/api/reviews/{id}", // DELETE review
                                "/api/business/{id}" // PATCH approve business
                        ).hasAuthority(UserRole.ADMIN.name())

                        .anyRequest().authenticated() // Fallback for any other authenticated requests
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 ->
                        oauth2.successHandler(oAuth2SuccessHandler)
                )
                .build();
    }
}
