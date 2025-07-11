package com.enigmacamp.pawtner.config;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initAdminUser() {
        if (!userRepository.existsByEmail("admin@pawtner.com")) {
            User adminUser = User.builder()
                    .email("admin@pawtner.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .name("Admin")
                    .address("EnigmaCamp")
                    .isVerified(true)
                    .role(UserRole.ADMIN)
                    .isEnabled(true)
                    .isCredentialsNonExpired(true)
                    .isAccountNonLocked(true)
                    .isAccountNonExpired(true)
                    .build();

            userRepository.save(adminUser);

            System.out.println("User admin berhasil dibuat email: " + adminUser.getEmail() + " Password: admin123");
        }
    }
}