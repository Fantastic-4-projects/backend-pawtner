
package com.enigmacamp.pawtner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "prescription_items")
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @NotBlank(message = "Medication name is required")
    @Size(max = 255)
    private String medicationName;

    @NotBlank(message = "Dosage is required")
    @Size(max = 255)
    private String dosage;

    @NotBlank(message = "Frequency is required")
    @Size(max = 255)
    private String frequency;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    @Lob
    private String instructions;
}
