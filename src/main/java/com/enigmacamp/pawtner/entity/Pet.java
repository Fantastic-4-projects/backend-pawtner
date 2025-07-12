package com.enigmacamp.pawtner.entity;

import com.enigmacamp.pawtner.constant.PetGender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "pet", fetch = FetchType.LAZY)
    @OrderBy("issueDate DESC")
    private List<Prescription> prescriptions;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String species;

    @Size(max = 100)
    private String breed;

    private LocalDate birthDate;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    private PetGender gender;

    @Size(max = 255)
    private String imageUrl;

    @Size(max = 2000)
    private String notes;

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
