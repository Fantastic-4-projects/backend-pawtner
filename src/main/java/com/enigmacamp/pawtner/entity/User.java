
package com.enigmacamp.pawtner.entity;

import com.enigmacamp.pawtner.constant.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255)
    @Column(unique = true, nullable = false)
    private String email;

    @Size(max = 50)
    @Column(unique = true)
    private String phoneNumber;

    @Size(max = 255)
    private String passwordHash;

    @Lob
    private String address;

    @Size(max = 255)
    private String imageUrl;

    @Builder.Default
    private Boolean isVerified = false;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder.Default
    @Column(nullable = false)
    private String authProvider = "local";

    @Size(max = 255)
    private String providerId;

    @Size(max = 20)
    private String codeVerification;

    private LocalDateTime codeExpire;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
