
package com.enigmacamp.pawtner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String species;

    @Size(max = 100)
    private String breed;

    private LocalDate birthDate;

    @Size(max = 255)
    private String imageUrl;

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
