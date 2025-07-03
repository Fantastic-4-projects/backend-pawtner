package com.enigmacamp.pawtner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrescriptionItemResponseDTO {
    private String id;
    private String medicationName;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String instructions;
}
