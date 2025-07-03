package com.enigmacamp.pawtner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrescriptionResponseDTO {
    private String id;
    private PetResponseDTO pet;
    private BusinessResponseDTO issuingBusiness;
    private LocalDate issueDate;
    private String notes;
    private List<PrescriptionItemResponseDTO> prescriptionItems;
    private LocalDateTime createdAt;
}
